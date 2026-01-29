package br.com.leandrocoelho.backend.dto.request;

import br.com.leandrocoelho.backend.model.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TransactionRequestDto {

    @NotBlank(message = "A descrição é obrigatória")
    @Size(min = 3, max = 255, message = "A descrição deve ter entre 3 e 255 caracteres")
    private String description;

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    private BigDecimal amount;

    @NotNull(message = "A data é obrigatória")
    private LocalDate date;

    @NotNull(message = "O tipo de transação é obrigatório")
    private TransactionType type;

    //Cai em sem categoria caso o usuario nao categorize na hora
    private UUID categoryId;

    //Preenchido para simulacao
    private UUID scenarioId;

    // Novos campos opcionais para enriquecer a transação manual
    private String merchantName;
    private Integer currentInstallment;
    private Integer totalInstallments;
}
