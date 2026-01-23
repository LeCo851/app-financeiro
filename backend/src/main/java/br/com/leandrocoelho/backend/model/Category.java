package br.com.leandrocoelho.backend.model;

import br.com.leandrocoelho.backend.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id","name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String icon; //Ex: 'mdi-food' (Nome do ícone no frontend)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type; // INCOME OU EXPENSE

    // verifica se é uma categoria padrão do sistema
    public boolean isGlobal(){
        return this.userId == null;
    }
}
