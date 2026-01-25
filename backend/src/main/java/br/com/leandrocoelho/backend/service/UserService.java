package br.com.leandrocoelho.backend.service;


import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void registerUserIfNotExists(UUID userId, String email, String fullName){

        if(userRepository.existsById(userId)){
            return;
        }

        log.info("Primeiro acesso detectado. Criando perfil local para: {} ", userId);

        User newUser = User.builder()
                .id(userId)
                .email(email != null ? email : "email_nao_encontrado@temp.com")
                .fullName(fullName != null ? fullName : "novo usuário")
                .grossSalary(BigDecimal.ZERO)
                .netSalaryEstimate(BigDecimal.ZERO)
                .receivesPlr(false)
                .plrEstimate(BigDecimal.ZERO)
                .impulsivityLevel("UNKNOWN")
                .build();

        userRepository.save(newUser);
    }
}
