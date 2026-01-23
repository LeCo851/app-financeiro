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

    // Flattening: Dados da categoria "achatados" para facilitar o display
    private String categoryName;
    private String categoryIcon;

    private String scenarioName;
    private boolean isSimulation;


    // Construtor estático (Factory Method) para converter Entidade -> DTO
    // Evita usar bibliotecas pesadas como ModelMapper para algo simples
    public static TransactionResponseDto fromEntity(Transaction transaction){
        TransactionResponseDto dto = new TransactionResponseDto();

        dto.setId(transaction.getId());
        dto.setDescription(transaction.getDescription());
        dto.setAmount(transaction.getAmount());
        dto.setDate(transaction.getDate());
        dto.setType(transaction.getType());
        dto.setSource(transaction.getSource());

        if(transaction.getCategory() != null){
            dto.setCategoryName(transaction.getCategory().getName());
            dto.setCategoryIcon(transaction.getCategory().getIcon());
        }

        if(transaction.getScenario() != null){
            dto.setScenarioName(transaction.getScenario().getName());
            dto.setSimulation(true);
        }else {
            dto.setSimulation(false);
        }
        return dto;
    }
}
