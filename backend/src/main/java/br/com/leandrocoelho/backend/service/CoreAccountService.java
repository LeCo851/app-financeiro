package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.integration.pluggy.dto.PluggyAccountDto;
import br.com.leandrocoelho.backend.model.Account;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CoreAccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Account syncAccount(PluggyAccountDto dto, User user){
        return accountRepository.findByPluggyAccountId(dto.id())
                .map(existing -> updateAccount(existing, dto))
                .orElseGet(() ->createNewAccount(dto, user));

    }

    private Account createNewAccount(PluggyAccountDto dto, User user){
            return accountRepository.save(Account.builder()
                    .user(user)
                    .pluggyAccountId(dto.id())
                    .name(dto.name())
                    .type(dto.type())
                    .subtype(dto.subtype())
                    .balance(dto.balance())
                    .currencyCode(dto.currencyCode())
                    .creditLimit(extractCreditLimit(dto))
                    .build());
    }

    private Account updateAccount(Account account, PluggyAccountDto dto){
        account.setName(dto.name());
        account.setType(dto.type());
        account.setSubtype(dto.subtype());
        account.setBalance(dto.balance());
        account.setCurrencyCode(dto.currencyCode());
        account.setCreditLimit(extractCreditLimit(dto));

        return accountRepository.save(account);
    }

    private BigDecimal extractCreditLimit(PluggyAccountDto dto) {
        return dto.creditData() != null ? dto.creditData().creditLimit() : null;
    }
}


