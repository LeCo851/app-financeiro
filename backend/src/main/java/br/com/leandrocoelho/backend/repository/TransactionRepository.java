package br.com.leandrocoelho.backend.repository;

import br.com.leandrocoelho.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findById(UUID id);
    // --- LISTAGENS (Mantidas) ---
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.scenario WHERE t.user.id = :userId ORDER BY t.date DESC")
    List<Transaction> findByUser_IdOrderByDateDesc(@Param("userId") UUID userId);

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.scenario WHERE t.user.id = :userId AND t.date >= :startDate AND t.date <= :endDate ORDER BY t.date DESC")
    List<Transaction> findByUser_IdAndDateBetweenOrderByDateDesc(@Param("userId") UUID userId, @Param("startDate") ZonedDateTime startDate, @Param("endDate") ZonedDateTime endDate);

    // --- CÁLCULOS FINANCEIROS CORRIGIDOS (Usando Sinal) ---

    // 1. Saldo Atual: Apenas soma tudo. Se gastou é negativo, se ganhou é positivo.
    // Ignora INVESTIMENTOS se você considera investimento como "dinheiro guardado" e não "gasto".
    // Mas para "Saldo em Conta Corrente", investimento é saída.
    // Vamos somar TUDO. O que define o saldo é a realidade do banco.
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.date <= :date")
    BigDecimal calculateBalanceUntilDate(@Param("userId") UUID userId, @Param("date") ZonedDateTime date);

    // 4. Média Mensal (Corrigida para usar apenas INCOME positivo)
    @Query(value = """
        SELECT COALESCE(AVG(monthly_total), 0)
        FROM (
            SELECT SUM(amount) as monthly_total
            FROM transactions
            WHERE user_id = :userId 
            AND type = 'INCOME'
            AND amount > 0 -- Garante que estornos não entrem na média
            GROUP BY date_trunc('month', date)
        ) as monthly_stats
    """, nativeQuery = true)
    BigDecimal calculateAverageMonthlyIncome(@Param("userId") UUID userId);

    // --- UTILITÁRIOS ---
    boolean existsByUser_IdAndTransactionHash(UUID userId, String transactionHash);

    Optional<Transaction> findByPluggyTransactionId(String pluggyTransactionId);

    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.account.id = :accountId 
        AND t.amount = :amount 
        AND CAST(t.date AS LocalDate) = :date
        AND t.description = :description
    """)
    Optional<Transaction> findPotentialDuplicate(
            @Param("accountId") UUID accountId,
            @Param("amount") BigDecimal amount,
            @Param("date") LocalDate date,
            @Param("description") String description
    );
    List<Transaction> findByPluggyTransactionIdIn(Collection<String> pluggyTransactionIds);


    @Query("SELECT t.transactionHash FROM Transaction t WHERE t.user.id = :userId AND t.transactionHash IN :hashes")
    Set<String> findHashesByUserAndHashesIn(@Param("userId") UUID userId, @Param("hashes") Collection<String> hashes);


    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.type = 'INCOME' " +
            "AND t.amount > 0 " +
            "AND t.date >= :start AND t.date <= :end")
    BigDecimal sumIncomeByUserIdAndDate(@Param("userId") UUID userId,
                                        @Param("start") ZonedDateTime start,
                                        @Param("end") ZonedDateTime end);

    // SOMA DESPESAS NO PERÍODO (Considerando apenas negativos)
    // Nota: O resultado será negativo (ex: -500). O Frontend decide se mostra com sinal ou não.
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.type = 'EXPENSE' " +
            "AND t.amount < 0 " +
            "AND t.date >= :start AND t.date <= :end")
    BigDecimal sumExpenseByUserIdAndDate(@Param("userId") UUID userId,
                                         @Param("start") ZonedDateTime start,
                                         @Param("end") ZonedDateTime end);

    // SALDO DO PERÍODO (Fluxo de Caixa Mensal: Receita + Despesa + Transferencias do mês)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.date >= :start AND t.date <= :end")
    BigDecimal sumPeriodBalance(@Param("userId") UUID userId,
                                @Param("start") ZonedDateTime start,
                                @Param("end") ZonedDateTime end);


    @Query(
            """
        SELECT t FROM Transaction t
        WHERE t.user.id = :userId
            AND t.isFixedExpense = TRUE 
            AND LOWER(t.description) = LOWER(:description)
            AND t.date >= :cutoffDate
        ORDER BY t.date DESC 
        LIMIT 1
""")
    Optional<Transaction> findRecurringPattern(
            @Param("userId") UUID userId,
            @Param("description") String description,
            @Param("cutoffDate") LocalDate cutoffDate
    );

    @Query("""
        SELECT COALESCE(SUM (t.amount),0)
        FROM Transaction t
        WHERE t.user.id = :userId
            AND t.date >= :start AND t.date <= :end
            AND t.type = 'EXPENSE'
            AND t.isFixedExpense = TRUE
""")
    BigDecimal sumFixedExpenses(
            @Param("userId") UUID userId,
            @Param("start") ZonedDateTime start,
            @Param("end") ZonedDateTime end
    );
}
