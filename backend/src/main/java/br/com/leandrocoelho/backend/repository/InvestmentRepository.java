package br.com.leandrocoelho.backend.repository;

import br.com.leandrocoelho.backend.model.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestmentRepository  extends JpaRepository<Investment, UUID> {
    
    List<Investment> findAllByUserId(UUID userId);
    List<Investment> findAllByUserIdAndStatus(UUID userId, String status);
    Optional<Investment> findByPluggyInvestmentId(String pluggyInvestmentId);

    @Query("SELECT COALESCE(SUM(i.balance),0)  FROM Investment i WHERE  i.user.id = :userId AND UPPER(i.status) ='ACTIVE' ")
    BigDecimal sumTotalActiveInvestments(@Param("userId") UUID userId);
    
}
