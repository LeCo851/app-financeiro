package br.com.leandrocoelho.backend.repository.projection;

import java.math.BigDecimal;

public interface FinancialHealthProjection {

    BigDecimal getNetSalary();
    BigDecimal getFixedExpenses();
    BigDecimal getSafeToSpend();
    Double getCommitmentPercentage();
}
