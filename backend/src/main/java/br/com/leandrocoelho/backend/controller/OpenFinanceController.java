package br.com.leandrocoelho.backend.controller;

import br.com.leandrocoelho.backend.dto.request.SyncRequestDto;
import br.com.leandrocoelho.backend.service.SyncService;
import br.com.leandrocoelho.backend.service.integration.PluggyService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/open-finance")
@RequiredArgsConstructor
public class OpenFinanceController {

    private final PluggyService pluggyService;
    private final SyncService syncService;

    @GetMapping("/connect-token")
    public ResponseEntity<Map<String, String>> getConnectToken(){
        String token = pluggyService.createConnectToken();
        return ResponseEntity.ok(Map.of("accessToken", token));
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncConnection(
            @RequestBody SyncRequestDto request,
            @AuthenticationPrincipal Jwt jwt
            ){
        log.info("ENDPOINT /SYNC CHAMADO!");
        log.info("DTO Recebido: getItemId() = {}", request.getItemId());

        if (request.getItemId() == null || request.getItemId().isEmpty()) {
            log.error("ERRO: O itemId veio nulo. Verifique o JSON enviado.");
            return ResponseEntity.badRequest().build();
        }

        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        log.info("Usuário identificado: {}", userId);

        syncService.syncConnection(request.getItemId(), userId);

        return ResponseEntity.ok().build();
    }
}
