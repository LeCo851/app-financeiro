package br.com.leandrocoelho.backend.model;

import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity{

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String description;

    @Column(name = "original_description")
    private String originalDescription;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // relacionamento com categoria ( 1 T -> N categorias)
    //Lazy para performance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @Column(name = "transaction_hash", unique = true)
    private String transactionHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionSource source;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayLoad;

    public boolean isSimulation(){
        return this.scenario != null;
    }


}
