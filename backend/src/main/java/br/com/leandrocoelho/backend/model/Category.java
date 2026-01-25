package br.com.leandrocoelho.backend.model;

import br.com.leandrocoelho.backend.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id","name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Category extends BaseEntity {

    // ALTERAÇÃO: Trocamos o UUID solto pelo Objeto User
    // Isso garante a integridade referencial (Foreign Key)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String icon; // Ex: 'mdi-food'

    // ADIÇÃO: O campo cor estava no SQL, vamos mapear aqui
    @Column(length = 20)
    private String color; // Ex: '#FF0000' ou 'red'

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type; // INCOME OU EXPENSE

    // Verifica se é uma categoria padrão do sistema (sem dono)
    public boolean isGlobal() {
        return this.user == null;
    }
}
