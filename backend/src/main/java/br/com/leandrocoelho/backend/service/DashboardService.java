package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.dto.response.CategoryExpenseDto;
import br.com.leandrocoelho.backend.dto.response.DashboardSummaryDto;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.repository.AccountRepository;
import br.com.leandrocoelho.backend.repository.TransactionRepository;
import br.com.leandrocoelho.backend.repository.UserRepository;
import br.com.leandrocoelho.backend.repository.projection.FinancialHealthProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository repository;
    private final UserRepository userRepository;

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

        FinancialHealthProjection healthProjection = userRepository.calculateFinancialHealth(userId, startZoned, endZoned);

        BigDecimal safeToSpend = (healthProjection != null) ? healthProjection.getSafeToSpend() : BigDecimal.ZERO;

        log.info("Valor disponível para gastar: {}", safeToSpend);

        Double commitmentPct = (healthProjection != null) ? healthProjection.getCommitmentPercentage() : 0.0;

        BigDecimal fixedExpense = (healthProjection != null) ? healthProjection.getFixedExpenses() : BigDecimal.ZERO;

        List<CategoryExpenseDto> topCategories = repository.findTopExpenseCategories(userId, startZoned, endZoned);
        if (topCategories == null) topCategories = Collections.emptyList();

        List<Transaction> recentTransactions = repository.findTop5ByUserIdOrderByDateDesc(userId);
        if(recentTransactions == null) recentTransactions = Collections.emptyList();
        // 3. Monta o DTO
        return DashboardSummaryDto.builder()
                .currentBalance(currentBalance)      // Card Saldo Corrente
                .monthBalance(monthBalance)          // Card Saldo do Mês
                .totalIncome(monthIncome)            // Card Receitas
                .totalExpense(monthExpense)          // Card Despesas
                .averageIncome(repository.calculateAverageMonthlyIncome(userId)) // Card Salário Médio
                .totalFixedExpense(fixedExpense) // gastos fixos
                .safeToExpend(safeToSpend)
                .commitmentPct(commitmentPct)
                .topExpenseCategories(topCategories)
                .recentTransactions(recentTransactions)
                .build();
    }

}
