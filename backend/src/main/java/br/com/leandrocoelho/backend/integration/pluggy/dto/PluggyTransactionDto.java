package br.com.leandrocoelho.backend.integration.pluggy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PluggyTransactionDto (
        String id,
        String description,
        BigDecimal amount,
        ZonedDateTime date,
        @JsonProperty("category") String categoryName,
        @JsonProperty("type") String type //"CREDIT" ou "DEBIT"
) {
}
