package br.com.leandrocoelho.backend.integration.pluggy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PluggyAccountDto(
        String id,
        String name,
        String number,
        @JsonProperty("balance") Double balance,
        @JsonProperty("currency_code") Double currencyCode
) {}

