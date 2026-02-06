package br.com.leandrocoelho.backend.dto.response;

import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.model.enums.TransactionType;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.UUID;

@Data
public class TransactionResponseDto {
    private UUID id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private TransactionType type;
    private TransactionSource source;
    private String categoryColor;
    // Flattening: Dados da categoria "achatados" para facilitar o display
    private String categoryName;
    private String categoryIcon;

    private String scenarioName;
    private boolean isSimulation;
    private String merchantName;
    private Integer currentInstallment;
    private Integer totalInstallments;
    private boolean fixed;


    // Construtor estático (Factory Method) para converter Entidade -> DTO
    public static TransactionResponseDto fromEntity(Transaction transaction){
        TransactionResponseDto dto = new TransactionResponseDto();

        dto.setId(transaction.getId());
        dto.setDescription(transaction.getDescription());
        dto.setAmount(transaction.getAmount());

        // --- A CORREÇÃO ESTÁ AQUI ---
        // Extraímos apenas a parte da Data do ZonedDateTime
        if (transaction.getDate() != null) {
            dto.setDate(transaction.getDate().toLocalDate());
        }
        // ----------------------------

        dto.setType(transaction.getType());
        dto.setSource(transaction.getSource());

        // Mapeamento de Categoria
        if(transaction.getCategory() != null){
            dto.setCategoryName(transaction.getCategory().getName());
            dto.setCategoryIcon(transaction.getCategory().getIcon());
            dto.setCategoryColor(transaction.getCategory().getColor());
        }

        // Mapeamento de Cenário
        if(transaction.getScenario() != null){
            dto.setScenarioName(transaction.getScenario().getName());
            dto.setSimulation(true);
        } else {
            dto.setSimulation(false);
        }

        // Mapeamento de Dados Ricos (Útil para o Front mostrar "Uber - Loja")
        dto.setMerchantName(transaction.getMerchantName());
        dto.setCurrentInstallment(transaction.getInstallmentNumber());
        dto.setTotalInstallments(transaction.getTotalInstallments());
        dto.setFixed(transaction.isFixedExpense());

        return dto;
    }
}
