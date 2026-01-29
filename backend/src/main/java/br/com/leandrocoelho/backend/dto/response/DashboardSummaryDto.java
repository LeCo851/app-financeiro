package br.com.leandrocoelho.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance; // Saldo do período (Mês/Ano)
    private BigDecimal currentBalance; // Saldo acumulado até hoje (Saldo Corrente)

}
