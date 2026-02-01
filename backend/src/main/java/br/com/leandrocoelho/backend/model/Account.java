package br.com.leandrocoelho.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "pluggy_account_id", nullable = false, unique = true)
    private String pluggyAccountId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String subtype;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "available_credit_limit")
    private BigDecimal availableCreditLimit;

    @Column(name = "agency")
    private String agency;

    @Column(name = "account_number")
    private String accountNumber;

    @CreationTimestamp
    private ZonedDateTime createdAt;
    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
