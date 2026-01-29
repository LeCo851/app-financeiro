package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.integration.pluggy.dto.PluggyAccountDto;
import br.com.leandrocoelho.backend.integration.pluggy.dto.PluggyTransactionDto;
import br.com.leandrocoelho.backend.integration.pluggy.mapper.PluggyDataMapper;
import br.com.leandrocoelho.backend.model.Account;
import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import br.com.leandrocoelho.backend.repository.AccountRepository;
import br.com.leandrocoelho.backend.repository.UserRepository;
import br.com.leandrocoelho.backend.service.integration.PluggyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PluggyDataMapper pluggyDataMapper;
    private final UserRepository userRepository;
    private final CoreAccountService accountService;
    private final CoreCategoryService coreCategoryService;

    @Transactional
    public void syncConnection(String itemId, UUID userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<PluggyAccountDto> accountDtos = pluggyService.getAccounts(itemId);

        for(PluggyAccountDto accountDto : accountDtos){

            Account account = accountService.syncAccount(accountDto, user);

            List<PluggyTransactionDto> pluggyTransactionDtos = pluggyService.getTransactions(accountDto.id());

            for(PluggyTransactionDto pluggyTransactionDto: pluggyTransactionDtos){
                try {
                    
                    Transaction transaction = pluggyDataMapper.toEntity(pluggyTransactionDto, user, account);
                    String categoryToUse = null;
                    if(pluggyTransactionDto.merchant() != null && pluggyTransactionDto.merchant().category() != null){
                        categoryToUse = pluggyTransactionDto.merchant().category();
                    }

                    Category category = coreCategoryService.findOrCreateCategory(
                            categoryToUse,
                            user,
                            transaction.getType()
                    );
                    transaction.setCategory(category);


                    coreTransactionService.createTransaction(transaction);
                }catch (Exception e){
                    log.error("Falha ao importar a transação {}: {}", pluggyTransactionDto.id(),e.getMessage());
                }
            }
        }
        log.info("Sincronização finalizada com sucesso");
    }
}
