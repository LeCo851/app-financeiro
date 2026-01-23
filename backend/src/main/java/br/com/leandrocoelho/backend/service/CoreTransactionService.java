package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.repository.TransactionRepository;
import br.com.leandrocoelho.backend.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoreTransactionService {

    private final TransactionRepository repository;

    @Transactional(readOnly = true)
    public List<Transaction> listMyTransactions(){

        UUID userId = UserContext.getCurrentUserId();
        return  repository.findAllByUserIdOrderByDateDesc(userId);
    }

    @Transactional
    public Transaction createTransaction(Transaction newTransaction){

        UUID userId = UserContext.getCurrentUserId();
        newTransaction.setUserId(userId);

        if(newTransaction.getDate().isAfter(LocalDate.now().plusYears(1))){
            throw new IllegalArgumentException("Não é permitido lançar transações muito futuras");
        }
        if(newTransaction.getTransactionHash() != null){
            boolean exists = repository.existsByUserIdAndTransactionHash(userId, newTransaction.getTransactionHash());
            if(exists){
                throw new IllegalStateException("Transação duplicada detectada.");
            }
        }
        return repository.save(newTransaction);
    }
}
