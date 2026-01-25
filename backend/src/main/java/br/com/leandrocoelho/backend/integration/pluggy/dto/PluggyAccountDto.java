package br.com.leandrocoelho.backend.integration.pluggy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PluggyAccountDto(
        String id,
        String name,
        String number,
        String type,    // "BANK", "CREDIT"
        String subtype, // "CHECKING_ACCOUNT", "CREDIT_CARD", "SAVINGS_ACCOUNT"
        BigDecimal balance,
        String currencyCode, // Corrigido de Double para String ("BRL")
        String itemId,

        // Objetos aninhados para dados específicos
        PluggyCreditDataDto creditData,
        PluggyBankDataDto bankData
) {

    // --- Sub-records para mapear os detalhes ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PluggyCreditDataDto(
            String level,       // "BLACK", "GOLD"
            String brand,       // "MASTERCARD", "VISA"
            BigDecimal creditLimit,
            BigDecimal availableCreditLimit,
            LocalDate balanceCloseDate,
            LocalDate balanceDueDate
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PluggyBankDataDto(
            String transferNumber, // Agência e conta formatados
            BigDecimal closingBalance
    ) {}
}