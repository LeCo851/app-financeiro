package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.integration.pluggy.dto.PluggyInvestmentDto;
import br.com.leandrocoelho.backend.model.Investment;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.repository.InvestmentRepository;
import br.com.leandrocoelho.backend.repository.UserRepository;
import br.com.leandrocoelho.backend.service.integration.PluggyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final PluggyService pluggyService;

    @Transactional
    public Investment create(Investment investment){
        return investmentRepository.save(investment);
    }

    @Transactional(readOnly = true)
    public List<Investment> listByUser(UUID userId){
        return investmentRepository.findAllByUserIdAndStatus(userId, "ACTIVE");
    }

    @Transactional
    public void syncInvestments(UUID userId, String pluggyItemId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<PluggyInvestmentDto> pluggyInvestmentDtos = pluggyService.getInvestments(pluggyItemId);

        for (PluggyInvestmentDto dto : pluggyInvestmentDtos){
            Investment investment = investmentRepository.findByPluggyInvestmentId(dto.id())
                    .orElseGet(() -> Investment.builder()
                            .user(user)
                            .pluggyInvestmentId(dto.id())
                            .build());

            // 1. Só sobrescreve o nome se for um investimento novo (ID nulo)
            // Isso impede que edições manuais do usuário sejam perdidas no próximo sync
            if (investment.getId() == null) {
                investment.setName(dto.name());
            }

            investment.setCode(dto.code());
            investment.setIsin(dto.isin());
            investment.setType(dto.type());
            investment.setSubType(dto.subtype());
            investment.setBalance(dto.balance());
            investment.setQuantity(dto.quantity());
            investment.setAmountInvested(dto.amount());
            investment.setAnnualRate(dto.annualRate());
            investment.setRateType(dto.rateType());
            investment.setLast12mRate(dto.last12mRate());
            investment.setDueDate(dto.dueDate());
            investment.setStatus(dto.status());

            log.info("Investment synced: {} | User: {}", investment.getName(), userId);
            investmentRepository.save(investment);
        }

    }
}
