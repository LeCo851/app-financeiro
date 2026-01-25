package br.com.leandrocoelho.backend.integration.pluggy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PluggyTransactionDto(
        String id,
        String description,
        BigDecimal amount,
        ZonedDateTime date,
        String status, // POSTED, PENDING
        String currencyCode,

        @JsonProperty("type") String type, // CREDIT, DEBIT

        // Objetos Aninhados (Dados Ricos)
        PluggyMerchantDto merchant,
        PluggyPaymentDataDto paymentData,
        PluggyCreditCardMetadataDto creditCardMetadata
) {

    // --- SUB-RECORDS (Podem ficar no mesmo arquivo para organização) ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PluggyMerchantDto(
            String name,
            String businessName,
            String cnpj,
            String category
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PluggyPaymentDataDto(
            String paymentMethod, // BOLETO, PIX
            PluggyActorDto payer,
            PluggyActorDto receiver,
            PluggyBoletoMetadataDto boletoMetadata
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PluggyActorDto(
            String name,
            PluggyDocumentDto documentNumber
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PluggyDocumentDto(
            String type, // CPF, CNPJ
            String value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PluggyBoletoMetadataDto(
            String barcode,
            String digitableLine
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PluggyCreditCardMetadataDto(
            Integer installmentNumber,
            Integer totalInstallments,
            BigDecimal totalAmount,
            ZonedDateTime purchaseDate
    ) {}
}