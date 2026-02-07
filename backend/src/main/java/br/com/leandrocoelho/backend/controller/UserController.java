package br.com.leandrocoelho.backend.controller;

import br.com.leandrocoelho.backend.dto.request.UserUpdateDto;
import br.com.leandrocoelho.backend.dto.response.UserResponseDto;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserUpdateDto dto) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza campos
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getGrossSalary() != null) user.setGrossSalary(dto.getGrossSalary());
        if (dto.getNetSalaryEstimate() != null) user.setNetSalaryEstimate(dto.getNetSalaryEstimate());
        if (dto.getReceivesPlr() != null) user.setReceivesPlr(dto.getReceivesPlr());
        if (dto.getPlrEstimate() != null) user.setPlrEstimate(dto.getPlrEstimate());
        if (dto.getSavingsGoal() != null) user.setSavingsGoal(dto.getSavingsGoal());

        userRepository.save(user);

        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }
}
