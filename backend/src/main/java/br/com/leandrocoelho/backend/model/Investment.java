package br.com.leandrocoelho.backend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Investment extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String code;

    private String isin;

    @Column(nullable = false)
    private BigDecimal balance;

    private String type;

    @Column(name = "subtype")
    private String subType;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "annual_rate",precision = 10, scale = 4)
    private BigDecimal annualRate;

    @Column(name = "rate_type")
    private String rateType;

    @Column(name = "last_12m_rate",precision = 10, scale = 4)
    private BigDecimal last12mRate;

    private String status;

    @Column(precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "amount_invested")
    private BigDecimal amountInvested;

    @Column(name = "pluggy_investment_id", nullable = false)
    private String pluggyInvestmentId;



}
