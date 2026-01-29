package br.com.leandrocoelho.backend.dto.response;

import br.com.leandrocoelho.backend.model.Investment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class InvestmentResponseDto {

    private UUID id;
    private String pluggyInvestmentId;
    private String name;
    private String code;
    private String isin;
    private String type;
    private String subType;
    
    private BigDecimal balance;
    private BigDecimal amountInvested;
    private BigDecimal quantity;
    
    // Rentabilidade
    private BigDecimal annualRate;
    private String rateType;
    private BigDecimal last12mRate;
    
    private LocalDate dueDate;
    private String status;

    public static InvestmentResponseDto fromEntity(Investment entity) {
        InvestmentResponseDto dto = new InvestmentResponseDto();
        
        dto.setId(entity.getId());
        dto.setPluggyInvestmentId(entity.getPluggyInvestmentId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setIsin(entity.getIsin());
        dto.setType(entity.getType());
        dto.setSubType(entity.getSubType());
        dto.setBalance(entity.getBalance());
        dto.setAmountInvested(entity.getAmountInvested());
        dto.setQuantity(entity.getQuantity());
        dto.setAnnualRate(entity.getAnnualRate());
        dto.setRateType(entity.getRateType());
        dto.setLast12mRate(entity.getLast12mRate());
        dto.setDueDate(entity.getDueDate());
        dto.setStatus(entity.getStatus());

        return dto;
    }
}