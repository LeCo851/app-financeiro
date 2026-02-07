package br.com.leandrocoelho.backend.controller;


import br.com.leandrocoelho.backend.dto.request.TransactionRequestDto;
import br.com.leandrocoelho.backend.dto.request.TransactionUpdateDto;
import br.com.leandrocoelho.backend.dto.response.DashboardSummaryDto;
import br.com.leandrocoelho.backend.dto.response.TransactionResponseDto;
import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.model.Scenario;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.repository.UserRepository;
import br.com.leandrocoelho.backend.service.CoreTransactionService;
import br.com.leandrocoelho.backend.service.DashboardService;
import br.com.leandrocoelho.backend.service.InvestmentService;
import br.com.leandrocoelho.backend.service.SyncService;
import groovyjarjarantlr4.v4.codegen.model.Sync;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final CoreTransactionService coreTransactionService;
    private final UserRepository userRepository;
    private final InvestmentService investmentService;
    private final SyncService sync;
    private final DashboardService dashboardService;
    
    @PostMapping
    public ResponseEntity<TransactionResponseDto> create
            (@RequestBody @Valid TransactionRequestDto dto,
             @AuthenticationPrincipal Jwt jwt
             ){

        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Transaction.TransactionBuilder builder = Transaction.builder()
                .user(user)
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .date(dto.getDate().atStartOfDay(ZoneId.systemDefault()))
                .type(dto.getType())
                .source(TransactionSource.MANUAL)
                // Novos campos
                .merchantName(dto.getMerchantName())
                .installmentNumber(dto.getCurrentInstallment())
                .totalInstallments(dto.getTotalInstallments());


        if(dto.getCategoryId() != null){
            builder.category(Category.builder().id(dto.getCategoryId()).build());
        }
        if(dto.getScenarioId() != null){
            builder.scenario(Scenario.builder().id(dto.getScenarioId()).build());
        }

        Transaction entity = builder.build();


        Transaction saved = coreTransactionService.createTransaction(entity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TransactionResponseDto.fromEntity(saved));

    }


    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ){
        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));

        List<Transaction> transactions = coreTransactionService.listTransactionsByUserAndMonth(userId, year, month);

        //converte lista de entidades para lista de DTOs
        List<TransactionResponseDto> responseDto = transactions.stream()
                .map(TransactionResponseDto :: fromEntity)
                .toList();

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required= false) Integer year,
            @RequestParam(required = false) Integer month
    )

    {
        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        DashboardSummaryDto summaryDto = dashboardService.getDashboardSummary(userId,year,month);

        return ResponseEntity.ok(summaryDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> delete(@PathVariable UUID id){
        coreTransactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncData(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getPluggyItemId() == null) {
            return ResponseEntity.badRequest().build();
        }
        log.info("PluggyItemId: {} | User Id: {}", user.getPluggyItemId(), userId);
        sync.runFullSync(userId, user.getPluggyItemId());
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/link-pluggy")
    public ResponseEntity<Void> linkPluggyItem(@RequestBody Map<String, String> payload, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        String itemId = payload.get("itemId");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setPluggyItemId(itemId);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable UUID id,
            @RequestBody TransactionUpdateDto dto
            ){
        Transaction updatedTransatcion = coreTransactionService.updateTransaction(id, dto);
        return ResponseEntity.ok(updatedTransatcion);
    }

    @PatchMapping("/{id}/toggle-fixed")
    public ResponseEntity<Void> toggleFixedExpense(@PathVariable UUID id){
        coreTransactionService.toggleFixedExpense(id);
        return ResponseEntity.noContent().build();
    }
}
