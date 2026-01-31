package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.dto.response.DashboardSummaryDto;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import br.com.leandrocoelho.backend.repository.AccountRepository;
import br.com.leandrocoelho.backend.repository.TransactionRepository;
import br.com.leandrocoelho.backend.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreTransactionService {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;

    // ==================================================================================
    // MÉTODOS DE LEITURA (READ)
    // ==================================================================================

    @Transactional(readOnly = true)
    public List<Transaction> listMyTransactions() {
        UUID userId = UserContext.getCurrentUserId();
        return repository.findByUser_IdOrderByDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> listTransactionsByUser(UUID userId) {
        return repository.findByUser_IdOrderByDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> listTransactionsByUserAndMonth(UUID userId, Integer year, Integer month) {
        if (year == null || month == null) {
            return listTransactionsByUser(userId);
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        ZonedDateTime startZoned = start.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endZoned = end.atTime(23, 59, 59).atZone(ZoneId.systemDefault());

        return repository.findByUser_IdAndDateBetweenOrderByDateDesc(userId, startZoned, endZoned);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(UUID userId) {
        return repository.calculateBalanceUntilDate(userId, ZonedDateTime.now());
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageMonthlyIncome(UUID userId) {
        return repository.calculateAverageMonthlyIncome(userId);
    }


    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(UUID userId, Integer year, Integer month) {
        // 1. Define o intervalo de datas (Se não vier, pega o mês atual)
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        LocalDate start = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        ZonedDateTime startZoned = start.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endZoned = end.atTime(23, 59, 59).atZone(ZoneId.systemDefault());

        // 2. Executa as queries
        // Saldo das Contas (Saldo Real Bancário)
        List<String> types = List.of("CHECKING_ACCOUNT","SAVINGS_ACCOUNT");
        BigDecimal currentBalance = accountRepository.sumBalancesByUserIdAndTypes(userId,types);
        if (currentBalance == null) currentBalance = BigDecimal.ZERO;

        // Receitas do Mês
        BigDecimal monthIncome = repository.sumIncomeByUserIdAndDate(userId, startZoned, endZoned);

        // Despesas do Mês
        BigDecimal monthExpense = repository.sumExpenseByUserIdAndDate(userId, startZoned, endZoned);

        // Saldo do Mês (Fluxo de Caixa: O quanto sobrou ou faltou neste mês específico)
        BigDecimal monthBalance = repository.sumPeriodBalance(userId, startZoned, endZoned);

        // 3. Monta o DTO
        return DashboardSummaryDto.builder()
                .currentBalance(currentBalance)      // Card Saldo Corrente
                .monthBalance(monthBalance)          // Card Saldo do Mês
                .totalIncome(monthIncome)            // Card Receitas
                .totalExpense(monthExpense)          // Card Despesas
                .averageIncome(repository.calculateAverageMonthlyIncome(userId)) // Card Salário Médio
                .build();
    }

    // ==================================================================================
    // MÉTODOS DE ESCRITA (WRITE - UPSERT LOGIC)
    // ==================================================================================

    @Transactional
    public Transaction createTransaction(Transaction newTransaction) {
        // Wrapper para usar a lógica otimizada de lote mesmo para uma única transação
        return saveTransactionsBatch(List.of(newTransaction)).get(0);
    }

    /**
     * O Método Coração: Recebe uma lista de transações (seja da Pluggy, CSV ou Manual)
     * e decide inteligentemente se cria novas ou atualiza as existentes.
     */
    @Transactional
    public List<Transaction> saveTransactionsBatch(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return List.of();
        }

        UUID userId = transactions.get(0).getUser().getId();
        List<Transaction> toSave = new ArrayList<>();

        // 1. Identifica IDs externos (Pluggy) para fazer busca em lote (Batch Fetch)
        List<String> externalIds = transactions.stream()
                .map(Transaction::getPluggyTransactionId)
                .filter(Objects::nonNull)
                .toList();

        // 2. Carrega mapa de transações existentes para evitar N+1 selects
        Map<String, Transaction> existingMap = new HashMap<>();
        if (!externalIds.isEmpty()) {
            existingMap = repository.findByPluggyTransactionIdIn(externalIds).stream()
                    .collect(Collectors.toMap(Transaction::getPluggyTransactionId, Function.identity()));
        }

        // 3. Processamento
        // Usamos Sets para garantir que não duplicamos hashs dentro do próprio lote de inserção
        Set<String> processedManualHashes = new HashSet<>();

        for (Transaction tx : transactions) {
            validateUserOwnership(tx, userId);
            normalizeTransactionAmount(tx);
            // A) Lógica para Transações Externas (Pluggy) - UPSERT
            if (tx.getPluggyTransactionId() != null) {
                if (existingMap.containsKey(tx.getPluggyTransactionId())) {
                    // ATUALIZAÇÃO (UPDATE)
                    Transaction existing = existingMap.get(tx.getPluggyTransactionId());
                    updateExistingTransactionData(existing, tx);
                    toSave.add(existing);
                } else {
                    // NOVA (INSERT)
                    toSave.add(tx);
                }
            }
            // B) Lógica para Transações Manuais - INSERT COM VALIDAÇÃO DE DUPLICIDADE
            else {
                processManualTransaction(tx, userId, processedManualHashes);
                toSave.add(tx);
            }
        }

        return repository.saveAll(toSave);
    }

    @Transactional
    public void deleteTransaction(UUID transactionId) {
        UUID userId = UserContext.getCurrentUserId();
        Transaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Operação não permitida: A transação não pertence ao seu usuário");
        }
        repository.delete(transaction);
    }

    // ==================================================================================
    // MÉTODOS AUXILIARES (PRIVADOS)
    // ==================================================================================

    private void validateUserOwnership(Transaction tx, UUID batchUserId) {
        if (tx.getUser() == null || !tx.getUser().getId().equals(batchUserId)) {
            throw new IllegalArgumentException("Inconsistência de usuário no lote de transações.");
        }
    }

    private void updateExistingTransactionData(Transaction target, Transaction source) {
        // Atualiza campos core
        target.setDescription(source.getDescription());
        target.setAmount(source.getAmount());
        target.setDate(source.getDate());
        target.setStatus(source.getStatus());

        // Só atualiza categoria se a nova não for nula (preserva categorização manual antiga se a nova vier vazia)
        if (source.getCategory() != null) {
            target.setCategory(source.getCategory());
        }

        // Atualiza campos ricos / metadados
        target.setMerchantName(source.getMerchantName());
        target.setPaymentMethod(source.getPaymentMethod());
        target.setType(source.getType()); // Atualiza tipo caso a regra de negócio tenha mudado (Ex: Expense -> Investment)
    }

    private void processManualTransaction(Transaction tx, UUID userId, Set<String> processedHashes) {
        // Garante Hash
        if (tx.getTransactionHash() == null) {
            tx.setTransactionHash(UUID.randomUUID().toString());
        }

        String hash = tx.getTransactionHash();

        // 1. Verifica duplicidade dentro do próprio lote (Input sujo)
        if (processedHashes.contains(hash)) {
            throw new IllegalStateException("Transação manual duplicada detectada no lote de entrada: " + tx.getDescription());
        }

        // 2. Verifica duplicidade no banco
        if (repository.existsByUser_IdAndTransactionHash(userId, hash)) {
            throw new IllegalStateException("Transação manual duplicada detectada no banco: " + tx.getDescription());
        }

        processedHashes.add(hash);
    }
    private void normalizeTransactionAmount(Transaction tx) {
        // Se for DESPESA e o valor for POSITIVO, inverte o sinal
        if (tx.getType() == TransactionType.EXPENSE
                && tx.getAmount() != null
                && tx.getAmount().compareTo(BigDecimal.ZERO) > 0) {

            tx.setAmount(tx.getAmount().negate()); // Método .negate() é mais limpo que multiply(-1)
        }
    }

}