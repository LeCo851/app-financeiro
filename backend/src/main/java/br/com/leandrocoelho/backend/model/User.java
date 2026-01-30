package br.com.leandrocoelho.backend.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "pluggy_item_id")
    private String pluggyItemId;

    @Column(name = "gross_salary")
    private BigDecimal grossSalary;

    @Column(name = "net_salary_estimate")
    private BigDecimal netSalaryEstimate;

    @Column(name = "receives_plr")
    private Boolean receivesPlr;

    @Column(name = "plr_estimate")
    private BigDecimal plrEstimate;

    @Column(name = "savings_goal")
    private BigDecimal savingsGoal;

    // --- Perfil Comportamental (IA) ---
    @Column(name = "impulsivity_level")
    private String impulsivityLevel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

}
