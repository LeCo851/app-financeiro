package br.com.leandrocoelho.backend.repository;

import br.com.leandrocoelho.backend.model.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestmentRepository  extends JpaRepository<Investment, UUID> {
    
    List<Investment> findAllByUserId(UUID userId);
    List<Investment> findAllByUserIdAndStatus(UUID userId, String status);

    // Sugestão: Renomear para manter padrão com Account e Transaction
    Optional<Investment> findByPluggyInvestmentId(String pluggyInvestmentId);
    
}
