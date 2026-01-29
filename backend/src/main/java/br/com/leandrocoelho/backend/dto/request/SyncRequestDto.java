package br.com.leandrocoelho.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SyncRequestDto {
    @NotBlank(message = "O Item ID (Conexão) é obrigatório")
    private String itemId;
}