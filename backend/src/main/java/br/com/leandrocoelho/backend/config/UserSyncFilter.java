package br.com.leandrocoelho.backend.config;

import br.com.leandrocoelho.backend.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSyncFilter extends OncePerRequestFilter {

    private final UserService userService;

    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
            ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof  Jwt jwt){
            try{
                String sub = jwt.getClaimAsString("sub");
                UUID userId = UUID.fromString(sub);
                String email = jwt.getClaimAsString("email");
                String name = "Nome não identificado";

                Map<String, Object> userMetadata= jwt.getClaim("user_metadata");

                if(userMetadata != null && userMetadata.containsKey("full_name")){
                    name = (String) userMetadata.get("full_name");
                }
                log.debug("UserSyncFilter processando: ID={} | Email={} | Nome={}", userId, email, name);

                userService.registerUserIfNotExists(userId, email, name);
            }catch (Exception e){
                log.error("Erro no filtro de sincronização do usuário: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request,response);
    }
}
