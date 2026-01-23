package br.com.leandrocoelho.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public class UserContext {

    public static UUID getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken){

            String userIdStr = jwtAuthenticationToken.getToken().getClaimAsString("sub");
            if(userIdStr != null){
                return UUID.fromString(userIdStr);
            }
        }
        // descomentar em prod
        //throw new RuntimeException("Usuário não autenticado");
        return null;
    }

    public static String getCurrentUserEmail(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken){
            return jwtAuthenticationToken.getToken().getClaimAsString("email");
        }
        return null;
    }
}
