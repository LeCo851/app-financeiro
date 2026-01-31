package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.integration.pluggy.dto.PluggyAccountDto;
import br.com.leandrocoelho.backend.integration.pluggy.dto.PluggyTransactionDto;
import br.com.leandrocoelho.backend.model.Account;
import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import br.com.leandrocoelho.backend.repository.UserRepository;
import br.com.leandrocoelho.backend.service.integration.PluggyService;
import br.com.leandrocoelho.backend.service.rule.TransactionClassifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final PluggyService pluggyService;
    private final CoreTransactionService coreTransactionService;
    private final CoreAccountService accountService;
    private final CoreCategoryService coreCategoryService;
    private final InvestmentService investmentService; // <--- NOVO: Para sincronizar investimentos
    private final UserRepository userRepository;
    private final TransactionClassifier classifier;    // <--- NOVO: Para classificar inteligência

    /**
     * Ponto de entrada único para sincronização total (Chamado pelo Controller)
     */
    @Transactional
    public void runFullSync(UUID userId, String itemId) {
        log.info("Iniciando Full Sync para o usuário {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 1. Sincroniza Contas e Transações
        syncAccountsAndTransactions(user, itemId);

        // 2. Sincroniza Investimentos
        try {
            investmentService.syncInvestments(userId, itemId);
        } catch (Exception e) {
            log.error("Erro ao sincronizar investimentos (mas as transações foram salvas): {}", e.getMessage());
        }

        log.info("Sincronização finalizada com sucesso");
    }

    private void syncAccountsAndTransactions(User user, String itemId) {
        // 1. Busca Contas na Pluggy
        List<PluggyAccountDto> accountDtos = pluggyService.getAccounts(itemId);

        for (PluggyAccountDto accountDto : accountDtos) {
            try {
                // 2. Sincroniza/Salva a Conta localmente
                Account account = accountService.syncAccount(accountDto, user);

                // 3. Busca Transações dessa conta
                List<PluggyTransactionDto> pluggyTransactions = pluggyService.getTransactions(accountDto.id());

                if (pluggyTransactions.isEmpty()) continue;

                // 4. Mapeia DTOs para Entidades (Aplicando Regras de Negócio)
                List<Transaction> transactionsToSave = pluggyTransactions.stream()
                        .map(dto -> mapToEntity(dto, user, account))
                        .collect(Collectors.toList());

                // 5. Salva em Lote (Mais performático que salvar um por um)
                coreTransactionService.saveTransactionsBatch(transactionsToSave);

            } catch (Exception e) {
                log.error("Falha ao processar conta {}: {}", accountDto.id(), e.getMessage());
            }
        }
    }

    private Transaction mapToEntity(PluggyTransactionDto dto, User user, Account account) {
        // 1. Extração segura
        String description = dto.description();
        String merchantName = dto.merchant() != null ? dto.merchant().name() : "";
        String pluggyCategoryName = (dto.merchant() != null) ? dto.merchant().category() : null;

        // CORREÇÃO: Pega o valor BRUTO, sem abs() e sem inverter sinal manualmente.
        // Se a Pluggy mandar -50.00, salvamos -50.00. Se mandar 50.00, salvamos 50.00.
        BigDecimal amount = dto.amount() != null ? dto.amount() : BigDecimal.ZERO;

        // 2. Classificação Baseada no TIPO (CREDIT/DEBIT)
        // Passamos o valor apenas para o classificador ter contexto, mas a decisão principal é pelo TIPO
        TransactionType type = classifier.classify(dto.type(), description);

        // 3. Resolução de Categoria (Mantém a lógica da IA/Mapa)
        Category category = coreCategoryService.resolveCategory(
                description,
                merchantName,
                pluggyCategoryName,
                user,
                type
        );

        // 4. Construção
        return Transaction.builder()
                .user(user)
                .account(account)
                .pluggyTransactionId(dto.id())
                .source(TransactionSource.PLUGGY)
                .originalDescription(description)
                .description(description)
                .amount(amount) // <--- Valor original da API
                .date(dto.date())
                .status(dto.status())
                .type(type)
                .category(category)
                .merchantName(merchantName)
                .paymentMethod(dto.paymentData() != null ? dto.paymentData().paymentMethod() : null)
                .build();
    }
}