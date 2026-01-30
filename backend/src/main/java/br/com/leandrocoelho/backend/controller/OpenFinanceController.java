package br.com.leandrocoelho.backend.controller;

import br.com.leandrocoelho.backend.dto.request.SyncRequestDto;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.repository.UserRepository;
import br.com.leandrocoelho.backend.service.SyncService;
import br.com.leandrocoelho.backend.service.UserService;
import br.com.leandrocoelho.backend.service.integration.PluggyService;
import lombok.RequiredArgsConstructor;
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
    private final UserService userService;
    private final UserRepository userRepository;

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

        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("full_name");
        log.info("Requisiçao de sync recebida. ItemId {} | Usuario: {}",request.getItemId(), userId);

        userService.registerUserIfNotExists(userId,email,name);
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado após o registro"));
        if(request.getItemId() != null && !request.getItemId().equals(user.getPluggyItemId())){
            user.setPluggyItemId(request.getItemId());
            userRepository.save(user);
            log.info("Pluggy Item Id vinculado ao usuário com sucesso");
        }

        syncService.runFullSync(userId, request.getItemId());

        return ResponseEntity.ok().build();
    }
}
