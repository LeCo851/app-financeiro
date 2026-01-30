package br.com.leandrocoelho.backend.controller;

import br.com.leandrocoelho.backend.dto.request.InvestmentRequestDto;
import br.com.leandrocoelho.backend.dto.request.SyncRequestDto;
import br.com.leandrocoelho.backend.dto.response.InvestmentResponseDto;
import br.com.leandrocoelho.backend.model.Investment;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.repository.UserRepository;
import br.com.leandrocoelho.backend.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<InvestmentResponseDto> create(
            @RequestBody @Valid InvestmentRequestDto dto,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Investment investment = Investment.builder()
                .user(user)
                .pluggyInvestmentId(dto.getPluggyInvestmentId())
                .name(dto.getName())
                .code(dto.getCode())
                .isin(dto.getIsin())
                .type(dto.getType())
                .subType(dto.getSubType())
                .balance(dto.getBalance())
                .amountInvested(dto.getAmountInvested())
                .quantity(dto.getQuantity())
                .annualRate(dto.getAnnualRate())
                .rateType(dto.getRateType())
                .dueDate(dto.getDueDate())
                .status("ACTIVE")
                .build();

        Investment saved = investmentService.create(investment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(InvestmentResponseDto.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<InvestmentResponseDto>> list(@AuthenticationPrincipal Jwt jwt){
        UUID userid = UUID.fromString(jwt.getClaimAsString("sub"));
        List<Investment> investments = investmentService.listByUser(userid);

        return ResponseEntity.ok(investments.stream()
                .map(InvestmentResponseDto :: fromEntity)
                .toList());
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> sync(
            @RequestBody @Valid SyncRequestDto dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        log.info("Requisição de Sync recebida {}",userId);
        investmentService.syncInvestments(userId, dto.getItemId());
        return ResponseEntity.noContent().build();
    }
}
