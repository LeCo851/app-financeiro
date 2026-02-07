package br.com.leandrocoelho.backend.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserUpdateDto {
    private String fullName;
    private BigDecimal grossSalary;
    private BigDecimal netSalaryEstimate;
    private Boolean receivesPlr;
    private BigDecimal plrEstimate;
    private BigDecimal savingsGoal;
}
