package br.com.leandrocoelho.backend.integration.pluggy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PluggyInvestmentDto(
        String id,
        String name,
        String code,
        String isin,
        String type,
        String subtype,
        BigDecimal balance,
        BigDecimal quantity,
        BigDecimal amount, // Valor investido original
        BigDecimal annualRate,
        String rateType,
        BigDecimal last12mRate,
        LocalDate dueDate,
        String status
) {}