package br.com.leandrocoelho.backend.integration.pluggy.mapper;

import br.com.leandrocoelho.backend.integration.pluggy.dto.PluggyTransactionDto;
import br.com.leandrocoelho.backend.model.Account;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PluggyDataMapper {

    private final ObjectMapper objectMapper;

    public Transaction toEntity(PluggyTransactionDto dto, User user, Account account){

        TransactionType type = "DEBIT".equalsIgnoreCase(dto.type())
                ? TransactionType.EXPENSE
                : TransactionType.INCOME;

        String jsonPayload = "{}";

        try {
            jsonPayload = objectMapper.writeValueAsString(dto);
        }catch (JsonProcessingException e){
            log.error("Erro ao serializar payload da transação {}", dto.id(), e);
        }

        Transaction.TransactionBuilder builder = Transaction.builder()
                .user(user)
                .account(account)
                .description(dto.description())
                .amount(dto.amount().abs())
                .date(dto.date())
                .type(type)
                .source(TransactionSource.PLUGGY)
                .status(dto.status())
                .pluggyTransactionId(dto.id())
                .transactionHash(dto.id())
                .rawPayLoad(jsonPayload);

        mapMerchant(builder, dto);
        mapCreditCard(builder, dto);
        mapPaymentData(builder, dto);

        return builder.build();
    }
    private void mapMerchant(Transaction.TransactionBuilder builder, PluggyTransactionDto dto) {
        if (dto.merchant() != null) {

            String bestName = dto.merchant().name();
            if(bestName == null || bestName.isBlank()){
                bestName = dto.merchant().businessName();
            }
            builder.merchantName(bestName);
            builder.merchantCnpj(dto.merchant().cnpj());
            builder.merchantCategory(dto.merchant().category());
        }
    }
    private void mapCreditCard(Transaction.TransactionBuilder builder, PluggyTransactionDto dto) {
        if (dto.creditCardMetadata() != null) {
            builder.installmentNumber(dto.creditCardMetadata().installmentNumber());
            builder.totalInstallments(dto.creditCardMetadata().totalInstallments());
        }
    }

    private void mapPaymentData(Transaction.TransactionBuilder builder, PluggyTransactionDto dto) {
        if (dto.paymentData() != null) {
            builder.paymentMethod(dto.paymentData().paymentMethod());

            // --- CORREÇÃO NO PAYER (PAGADOR) ---
            if (dto.paymentData().payer() != null) {
                // Só tenta pegar o valor se o documento não for nulo
                if (dto.paymentData().payer().documentNumber() != null) {
                    builder.payerDocNumber(dto.paymentData().payer().documentNumber().value());
                }
            }
            // --- CORREÇÃO NO RECEIVER (RECEBEDOR) ---
            if (dto.paymentData().receiver() != null) {
                builder.receiverName(dto.paymentData().receiver().name());

                // Só tenta pegar o valor se o documento não for nulo
                if (dto.paymentData().receiver().documentNumber() != null) {
                    builder.receiverDocNumber(dto.paymentData().receiver().documentNumber().value());
                }
            }

            if (dto.paymentData().boletoMetadata() != null) {
                builder.boletoBarcode(dto.paymentData().boletoMetadata().barcode());
            }
        }
    }
}
