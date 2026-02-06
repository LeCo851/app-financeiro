package br.com.leandrocoelho.backend.model;

import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

import java.time.ZonedDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Transaction extends BaseEntity {

    // --- RELACIONAMENTOS (Alterado de UUID raw para Entidades) ---

    // Usamos o objeto User. Isso permite fazer transaction.getUser().getGrossSalary() na IA.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // NOVO: Vínculo com a conta bancária (Essencial para saber se é Crédito ou Débito real)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    // --- DADOS BÁSICOS ---

    @Column(nullable = false)
    private String description;

    @Column(name = "original_description")
    private String originalDescription;

    @Column(nullable = false)
    private BigDecimal amount;

    // Correto: ZonedDateTime mapeia para TIMESTAMP WITH TIME ZONE do PostgreSQL
    @Column(nullable = false)
    private ZonedDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // CREDIT, DEBIT

    // NOVO: Status da transação (PENDING ou POSTED)
    @Column(length = 20)
    private String status;

    @Column(name = "currency_code", length = 3)
    private String currencyCode; // BRL, USD

    // --- IDENTIFICADORES ---

    @Column(name = "transaction_hash")
    private String transactionHash;

    // NOVO: ID original da Pluggy (Crucial para atualizações e não duplicar dados)
    @Column(name = "pluggy_transaction_id", unique = true)
    private String pluggyTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionSource source; // PLUGGY, MANUAL

    // --- DADOS RICOS (MERCHANT / LOJA) ---
    // A IA usa isso para saber se você gasta muito em "Restaurantes" vs "Supermercado"

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "merchant_cnpj", length = 20)
    private String merchantCnpj;

    @Column(name = "merchant_category")
    private String merchantCategory;

    // --- DADOS DE PAGAMENTO (BOLETO / PIX) ---

    @Column(name = "payment_method")
    private String paymentMethod; // BOLETO, PIX

    @Column(name = "payer_doc_number", length = 20)
    private String payerDocNumber;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_doc_number", length = 20)
    private String receiverDocNumber;

    @Column(name = "boleto_barcode")
    private String boletoBarcode;

    // --- PARCELAMENTO (CARTÃO DE CRÉDITO) ---
    // Vital para projeção de fluxo de caixa futuro

    @Column(name = "installment_number")
    private Integer installmentNumber; // Ex: Parcela 2

    @Column(name = "total_installments")
    private Integer totalInstallments; // Ex: De 10

    // --- PAYLOAD BRUTO (BACKUP) ---

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayLoad;

    @Column(name = "manual_edit",nullable = false)
    private boolean manualEdit = false;

    @Column(name = "is_fixed_expense" ,nullable = false)
    private boolean isFixedExpense = false;

    // --- MÉTODOS UTILITÁRIOS ---

    public boolean isSimulation() {
        return this.scenario != null;
    }
}