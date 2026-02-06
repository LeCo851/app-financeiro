package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.dto.request.TransactionUpdateDto;
import br.com.leandrocoelho.backend.dto.response.DashboardSummaryDto;
import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import br.com.leandrocoelho.backend.repository.AccountRepository;
import br.com.leandrocoelho.backend.repository.CategoryRepository;
import br.com.leandrocoelho.backend.repository.TransactionRepository;
import br.com.leandrocoelho.backend.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;

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
    private final CategoryRepository categoryRepository;
    private final LocaleResolver localeResolver;

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

        BigDecimal fixedExpense = repository.sumFixedExpenses(userId, startZoned, endZoned);
        // 3. Monta o DTO
        return DashboardSummaryDto.builder()
                .currentBalance(currentBalance)      // Card Saldo Corrente
                .monthBalance(monthBalance)          // Card Saldo do Mês
                .totalIncome(monthIncome)            // Card Receitas
                .totalExpense(monthExpense)          // Card Despesas
                .averageIncome(repository.calculateAverageMonthlyIncome(userId)) // Card Salário Médio
                .totalFixedExpense(fixedExpense) // gastos fixos
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
   public List<Transaction> saveTransactionsBatch(List<Transaction> transactions){
       if (transactions == null || transactions.isEmpty()) return List.of();

       UUID userId= transactions.get(0).getUser().getId();
       List<Transaction> toSave = new ArrayList<>();
       Set<UUID> processedIds = new HashSet<>();

       Map<String, Transaction> existingPluggyMap = loadExistingTransactionsBatch(transactions);
       LocalDate recurrenceLookBack = LocalDate.now().minusDays(60);

       for(Transaction tx: transactions){
           validateUserOwnership(tx, userId);
           normalizeTransactionAmount(tx);

           if (tx.getPluggyTransactionId() != null){
               processPluggyTransaction(tx, existingPluggyMap, processedIds, recurrenceLookBack,toSave);
           }else {
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

    @Transactional
    public Transaction updateTransaction(UUID transactionId, TransactionUpdateDto dto){
        UUID userId = UserContext.getCurrentUserId();

        Transaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada"));
        if(!transaction.getUser().getId().equals(userId)){
           throw  new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a transação");
        }

        if(dto.type() != null && dto.type() != transaction.getType()){
            transaction.setType(dto.type());
            adjustAmountSignByType(transaction);
        }

        if(dto.categoryId() != null){
            if(transaction.getCategory() == null || !transaction.getCategory().getId().equals(dto.categoryId())){
                Category newCategory = categoryRepository.findById(dto.categoryId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));

                if(!newCategory.getUser().getId().equals(userId)){
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Essa categoria não pertence a você");
                }
                transaction.setCategory(newCategory);
            }
        }
        if(dto.description() != null && !dto.description().isBlank()){
            transaction.setDescription(dto.description());
        }

        transaction.setManualEdit(true);
        return repository.save(transaction);

    }

    // ==================================================================================
    // MÉTODOS AUXILIARES (PRIVADOS)
    // ==================================================================================
    private Map<String, Transaction> loadExistingTransactionsBatch(List<Transaction> transactions){
        List<String> externalIds = transactions.stream()
                .map(Transaction::getPluggyTransactionId)
                .filter(Objects::nonNull)
                .toList();
        if(externalIds.isEmpty()) return new HashMap<>();

        return repository.findByPluggyTransactionIdIn(externalIds).stream()
                .collect(Collectors.toMap(Transaction::getPluggyTransactionId,Function.identity()));
    }

    private void processPluggyTransaction(
            Transaction tx,
            Map<String, Transaction> existingMap,
            Set<UUID> processedIds,
            LocalDate recurrenceLookBack,
            List<Transaction> toSave){

        if(existingMap.containsKey(tx.getPluggyTransactionId())){
            Transaction existing = existingMap.get(tx.getPluggyTransactionId());
            updateExistingTransactionData(existing, tx);
            addToSaveList(existing, processedIds, toSave);
        }
    }

    private void handlePotentialNewPluggyTransaction(
            Transaction tx,
            Map<String, Transaction> existingMap,
            Set<UUID> processedIds,
            LocalDate recurrenceLookBack,
            List<Transaction> toSave
    ){
        Optional<Transaction> ghostDuplicate = repository.findPotentialDuplicate(
                tx.getAccount().getId(),
                tx.getAmount(),
                tx.getDate().toLocalDate(),
                tx.getDescription()
        );
        if (ghostDuplicate.isPresent()){
            Transaction existing = ghostDuplicate.get();
            log.info("Smart match: Atualizando ID de {} para {}", existing.getPluggyTransactionId(), tx.getPluggyTransactionId());

            existing.setPluggyTransactionId(tx.getPluggyTransactionId());
            updateExistingTransactionData(existing, tx);
            addToSaveList(existing, processedIds, toSave);
        }else {
            applyRecurrencePattern(tx, recurrenceLookBack);
            toSave.add(tx);
        }
    }

    private void applyRecurrencePattern(Transaction tx, LocalDate lookBackDate){
        repository.findRecurringPattern(tx.getUser().getId(), tx.getDescription(), lookBackDate)
                .ifPresent(pattern -> {
                    tx.setFixedExpense(true);
                    log.info("Recorrência: '{}' marcada como fixa (baseada em registro de {})", tx.getDescription(), pattern.getDate());
                });
    }

    private void addToSaveList(Transaction tx, Set<UUID> processedIds, List<Transaction> toSave){
        if(processedIds.add(tx.getId())){
            toSave.add(tx);
        }
    }


    private void adjustAmountSignByType(Transaction tx){
        BigDecimal amount = tx.getAmount();
        if(amount == null) return;

        switch (tx.getType()){
            case INCOME:
                if(amount.compareTo(BigDecimal.ZERO) < 0){
                    tx.setAmount(amount.abs());
                }
                break;
            case EXPENSE:
            case INVESTMENT:
                if (amount.compareTo(BigDecimal.ZERO) > 0){
                    tx.setAmount(amount.abs().negate());
                }
                break;
            case TRANSFER:
                break;
        }
    }

    private void validateUserOwnership(Transaction tx, UUID batchUserId) {
        if (tx.getUser() == null || !tx.getUser().getId().equals(batchUserId)) {
            throw new IllegalArgumentException("Inconsistência de usuário no lote de transações.");
        }
    }

    private void updateExistingTransactionData(Transaction target, Transaction source) {
        // 1. CAMPOS TÉCNICOS/BANCÁRIOS (Sempre atualizamos)
        // O banco sempre tem razão sobre a data real da compensação e o status (Pendente -> Postado)
        target.setDate(source.getDate());
        target.setStatus(source.getStatus());
        target.setPaymentMethod(source.getPaymentMethod());
        target.setMerchantName(source.getMerchantName());

        // 2. VERIFICAÇÃO DE PROTEÇÃO (Manual Edit)
        if (!target.isManualEdit()) {
            target.setDescription(source.getDescription());
            target.setCategory(source.getCategory());
            target.setType(source.getType());
            target.setAmount(source.getAmount()); // Aceita o valor e sinal da API
        } else {

            BigDecimal newRawAmount = source.getAmount() != null ? source.getAmount().abs() : BigDecimal.ZERO;

            if (target.getType() == TransactionType.EXPENSE || target.getType() == TransactionType.INVESTMENT) {
                target.setAmount(newRawAmount.negate());
            } else {
                target.setAmount(newRawAmount);
            }
        }
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

    public void toggleFixedExpense(UUID id){
       Transaction tx = repository.findById(id)
               .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

       boolean newValue = !tx.isFixedExpense();
       tx.setFixedExpense(newValue);

       repository.save(tx);
        log.info("Transação '{}' (ID: {}) atualizada. Gasto Fixo: {}",
                tx.getDescription(), id, newValue);
    }

}