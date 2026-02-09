package br.com.leandrocoelho.backend.dto.response;

import java.math.BigDecimal;

public record CategoryExpenseDto(
        String name,
        BigDecimal amount
) {
}
