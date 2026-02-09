package br.com.leandrocoelho.backend.dto.response;

import br.com.leandrocoelho.backend.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance; // Saldo do período (Mês/Ano)
    private BigDecimal currentBalance; // Saldo acumulado até hoje (Saldo Corrente)
    private BigDecimal averageIncome;
    private BigDecimal monthBalance;
    private BigDecimal totalFixedExpense;
    private BigDecimal safeToExpend;
    private Double commitmentPct;
    List<CategoryExpenseDto> topExpenseCategories; // Onde o dinheiro está indo
    List<Transaction> recentTransactions;

}
