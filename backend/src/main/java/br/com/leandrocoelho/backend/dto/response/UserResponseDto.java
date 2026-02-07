package br.com.leandrocoelho.backend.dto.response;

import br.com.leandrocoelho.backend.model.User;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class UserResponseDto {
    private UUID id;
    private String fullName;
    private String email;
    private BigDecimal grossSalary;
    private BigDecimal netSalaryEstimate;
    private Boolean receivesPlr;
    private BigDecimal plrEstimate;
    private BigDecimal savingsGoal;

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .grossSalary(user.getGrossSalary())
                .netSalaryEstimate(user.getNetSalaryEstimate())
                .receivesPlr(user.getReceivesPlr())
                .plrEstimate(user.getPlrEstimate())
                .savingsGoal(user.getSavingsGoal())
                .build();
    }
}
