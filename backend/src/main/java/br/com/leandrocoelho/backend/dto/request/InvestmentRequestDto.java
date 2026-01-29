package br.com.leandrocoelho.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvestmentRequestDto {

    @NotBlank(message = "O ID externo (Pluggy ou Manual) é obrigatório")
    private String pluggyInvestmentId;

    @NotBlank(message = "O nome do investimento é obrigatório")
    private String name;

    private String code; // Ticker (PETR4)
    private String isin;
    private String type;    // Renda Fixa, Ações...
    private String subType; // CDB, LCI...

    @NotNull(message = "O saldo atual é obrigatório")
    private BigDecimal balance;

    @PositiveOrZero
    private BigDecimal amountInvested;
    private BigDecimal quantity;

    // Dados de rentabilidade/prazos são opcionais na entrada
    private BigDecimal annualRate;
    private String rateType;
    private LocalDate dueDate;
}
