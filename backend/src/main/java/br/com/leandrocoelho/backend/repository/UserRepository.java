package br.com.leandrocoelho.backend.repository;

import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.repository.projection.FinancialHealthProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {




    @Query(value = """
        SELECT
            up.net_salary_estimate AS netSalary,
            -- Soma das despesas fixas (retorna negativo, ex: -1500.00)
            COALESCE(SUM(t.amount), 0) AS fixedExpenses,
            -- Cálculo do Livre para Gastar (Salário + Despesa Negativa)
            (COALESCE(up.net_salary_estimate, 0) + COALESCE(SUM(t.amount), 0)) AS safeToSpend,
            -- Cálculo da Porcentagem de Comprometimento (Despesa Absoluta / Salário * 100)
            CASE
                WHEN up.net_salary_estimate > 0 THEN
                    (ABS(COALESCE(SUM(t.amount), 0)) / up.net_salary_estimate) * 100 
                ELSE 0 
            END AS commitmentPercentage

        FROM users up
        LEFT JOIN transactions t ON t.user_id = up.id 
            AND t.is_fixed_expense = TRUE
            AND t.type = 'EXPENSE'
            AND t.date BETWEEN :startDate AND :endDate
        WHERE up.id = :userId
        GROUP BY up.id, up.net_salary_estimate
    """, nativeQuery = true)
    FinancialHealthProjection calculateFinancialHealth(
            @Param("userId") UUID userId,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate
    );
}
