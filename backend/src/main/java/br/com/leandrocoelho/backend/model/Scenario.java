package br.com.leandrocoelho.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "scenarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario extends BaseEntity {

    @Column(name = "user_id",nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name; //Ex: viagem

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private  Boolean isActive = true;
}
