package br.com.leandrocoelho.backend.repository;

import br.com.leandrocoelho.backend.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByPluggyAccountId(String pluggyAccountId);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user.id = :userId AND a.subtype IN :subtypes")
    BigDecimal sumBalancesByUserIdAndTypes(@Param("userId") UUID userId, @Param("subtypes") List<String> subtypes);
}