package br.com.leandrocoelho.backend.controller;


import br.com.leandrocoelho.backend.dto.request.TransactionRequestDto;
import br.com.leandrocoelho.backend.dto.response.TransactionResponseDto;
import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.model.Scenario;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.service.CoreTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final CoreTransactionService coreTransactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDto> create(@RequestBody @Valid TransactionRequestDto dto){

        Transaction entity = Transaction.builder()
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .type(dto.getType())
                .source(TransactionSource.MANUAL)
                .category(dto.getCategoryId() != null ? Category.builder().userId(dto.getCategoryId()).build() : null)
                .scenario(dto.getScenarioId() != null ? Scenario.builder().userId(dto.getScenarioId()).build() : null)
                .build();

        Transaction saved = coreTransactionService.createTransaction(entity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TransactionResponseDto.fromEntity(saved));

    }


    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> list(){
        List<Transaction> transactions = coreTransactionService.listMyTransactions();

        //converte lista de entidades para lista de DTOs
        List<TransactionResponseDto> responseDto = transactions.stream()
                .map(TransactionResponseDto :: fromEntity)
                .toList();

        return ResponseEntity.ok(responseDto);
    }
}
