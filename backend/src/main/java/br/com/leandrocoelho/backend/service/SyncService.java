package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.integration.pluggy.dto.PluggyAccountDto;
import br.com.leandrocoelho.backend.integration.pluggy.dto.PluggyTransactionDto;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import br.com.leandrocoelho.backend.service.integration.PluggyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final PluggyService pluggyService;
    private final CoreTransactionService coreTransactionService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void syncConnection(String itemId, UUID userId){

        List<PluggyAccountDto> accountDtos = pluggyService.getAccounts(itemId);

        for(PluggyAccountDto accountDto : accountDtos){
            List<PluggyTransactionDto> pluggyTransactionDtos = pluggyService.getTransactions(accountDto.id());

            for(PluggyTransactionDto pluggyTransactionDto: pluggyTransactionDtos){

                TransactionType type = "DEBIT".equalsIgnoreCase(pluggyTransactionDto.type())
                        ? TransactionType.EXPENSE
                        : TransactionType.INCOME;

                String jsonPayload;
                try {
                    jsonPayload = objectMapper.writeValueAsString(pluggyTransactionDto);
                }catch (JsonProcessingException e){
                    log.error("Erro ao converter transação para JSON", e);
                    jsonPayload ="{}";
                }
                Transaction newTransaction = Transaction.builder()
                        .userId(userId)
                        .description(pluggyTransactionDto.description())
                        .amount(pluggyTransactionDto.amount().abs())
                        .date(pluggyTransactionDto.date().toLocalDate())
                        .type(type)
                        .source(TransactionSource.OPEN_FINANCE)
                        .transactionHash(pluggyTransactionDto.id())
                        .rawPayLoad(jsonPayload)
                        .build();

                try {
                    coreTransactionService.createTransaction(newTransaction);
                }catch (IllegalStateException e){
                    log.info("Transação já importada: ",  pluggyTransactionDto.description());
                }
            }
        }
    }
}
