package br.com.leandrocoelho.backend.repository;

import br.com.leandrocoelho.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {


    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.category
        LEFT JOIN FETCH t.scenario
        WHERE t.user.id = :userId
        ORDER BY t.date DESC
    """)
    List<Transaction> findByUser_IdOrderByDateDesc(@Param("userId") UUID userId);

    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.category
        LEFT JOIN FETCH t.scenario
        WHERE t.user.id = :userId
        AND t.date >= :startDate
        AND t.date <= :endDate
        ORDER BY t.date DESC
    """)
    List<Transaction> findByUser_IdAndDateBetweenOrderByDateDesc(
            @Param("userId") UUID userId,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate
    );


    boolean existsByUser_IdAndTransactionHash(UUID userId, String transactionHash);
    boolean existsByPluggyTransactionId(String pluggyTransactionId);
    //Query nativa otimizada para buscar somente o saldo
    @Query("SELECT COALESCE(SUM (t.amount),0 ) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'INCOME'")
    BigDecimal sumIncomeByUserId(UUID userId);

    @Query("SELECT COALESCE(SUM (t.amount),0 ) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE'")
    BigDecimal sumExpenseByUserId(UUID userId);

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN t.type = 'EXPENSE' THEN -t.amount 
                WHEN t.type = 'INCOME' THEN t.amount 
                ELSE 0 
            END
        ), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
        AND t.date <= :date
    """)
    BigDecimal calculateBalanceUntilDate(@Param("userId") UUID userId, @Param("date") ZonedDateTime date);

    Optional<Transaction> findByPluggyTransactionId(String pluggyTransactionId);
}
