package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.repository.TransactionRepository;
import br.com.leandrocoelho.backend.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoreTransactionService {

    private final TransactionRepository repository;

    @Transactional(readOnly = true)
    public List<Transaction> listMyTransactions(){

        UUID userId = UserContext.getCurrentUserId();
        return  repository.findByUser_IdOrderByDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> listTransactionsByUser(UUID userId) {
        return repository.findByUser_IdOrderByDateDesc(userId);
    }

    @Transactional
    public Transaction createTransaction(Transaction newTransaction){

        if(newTransaction.getUser() == null){
            throw new IllegalArgumentException("A transação deve estar vinculada a um usuário");
        }

        UUID userId = newTransaction.getUser().getId();

        if(newTransaction.getSource() == null){
            newTransaction.setSource(TransactionSource.MANUAL);
        }

        if(newTransaction.getTransactionHash() == null){
            newTransaction.setTransactionHash(UUID.randomUUID().toString());
        }

        if(newTransaction.getDate().isAfter(ZonedDateTime.now().plusYears(1))){
            throw new IllegalArgumentException("Não é permitido lançar transações muito futuras");
        }
        if(newTransaction.getTransactionHash() != null){
            boolean exists = repository.existsByUser_IdAndTransactionHash(userId, newTransaction.getTransactionHash());
            if(exists){
                throw new IllegalStateException("Transação duplicada detectada.");
            }
        }
        return repository.save(newTransaction);
    }
}
