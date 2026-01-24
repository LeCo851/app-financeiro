package br.com.leandrocoelho.backend.repository;

import br.com.leandrocoelho.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllByUserIdOrderByDateDesc(UUID userId);


    boolean existsByUserIdAndTransactionHash(UUID userId, String transactionHash);

    //Query nativa otimizada para buscar somente o saldo
    @Query("SELECT COALESCE(SUM (t.amount),0 ) FROM Transaction t WHERE t.userId = :userId AND t.type = 'INCOME'")
    BigDecimal sumIncomeByUserId(UUID userId);

    @Query("SELECT COALESCE(SUM (t.amount),0 ) FROM Transaction t WHERE t.userId = :userId AND t.type = 'EXPENSE'")
    BigDecimal sumExpenseByUserId(UUID userId);
}
