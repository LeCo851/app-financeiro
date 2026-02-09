package br.com.leandrocoelho.backend.controller;

import br.com.leandrocoelho.backend.service.ai.AdvisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/advisor")
@RequiredArgsConstructor
public class AdvisorController {

    private final AdvisorService advisorService;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAdvisor(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> payload) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        String question = payload.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("response", "Por favor, digite uma pergunta válida."));
        }
        String advice = advisorService.getAdvice(userId, question);

        return ResponseEntity.ok(Map.of("response",advice));
    }

}
