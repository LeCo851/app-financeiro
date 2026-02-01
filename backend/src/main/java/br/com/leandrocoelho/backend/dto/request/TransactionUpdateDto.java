package br.com.leandrocoelho.backend.dto.request;
import br.com.leandrocoelho.backend.model.enums.TransactionType;

import java.util.UUID;

public record TransactionUpdateDto(
        UUID categoryId,
        TransactionType type,
        String description
) {}
