package br.com.leandrocoelho.backend.service.rule;

import br.com.leandrocoelho.backend.model.enums.TransactionType;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class TransactionClassifier {

    /**
     * @param pluggyType "CREDIT" ou "DEBIT" (vindo direto da API)
     * @param description Descrição para refinar (achar investimentos/transferências)
     */
    public TransactionType classify(String pluggyType, String description) {

        String desc = description != null ? description.toUpperCase().trim() : "";
        boolean isCredit = "CREDIT".equalsIgnoreCase(pluggyType);

        // =============================================================
        // CENÁRIO 1: DINHEIRO ENTRANDO (CREDIT)
        // =============================================================
        if (isCredit) {
            // Se for pagamento de fatura entrando no cartão ou estorno -> TRANSFERÊNCIA
            if (isPaymentReceived(desc)) {
                return TransactionType.TRANSFER;
            }

            // Se for resgate de investimento -> TRANSFERÊNCIA (ou INCOME se for só juros, mas simplificamos)
            if (isInvestmentRedemption(desc)) {
                return TransactionType.TRANSFER;
            }

            // Padrão: RECEITA
            return TransactionType.INCOME;
        }

        // =============================================================
        // CENÁRIO 2: DINHEIRO SAINDO (DEBIT)
        // =============================================================
        else {
            // Se for dinheiro saindo para pagar a fatura -> TRANSFERÊNCIA
            if (isBillPayment(desc)) {
                return TransactionType.TRANSFER;
            }

            // Se for dinheiro saindo para Corretora/CDB -> INVESTIMENTO (Patrimônio)
            if (isInvestmentApplication(desc)) {
                return TransactionType.INVESTMENT;
            }

            // Padrão: DESPESA
            return TransactionType.EXPENSE;
        }
    }

    // --- REGRAS AUXILIARES ---

    private boolean isPaymentReceived(String desc) {
        return desc.contains("PAGAMENTO RECEBIDO") ||
                desc.contains("PAYMENT RECEIVED") ||
                desc.contains("CREDITO DE FATURA") ||
                desc.contains("ESTORNO");
    }

    private boolean isInvestmentRedemption(String desc) {
        return desc.contains("RESGATE") ||
                (desc.contains("PROVENTOS") && desc.contains("RESGATE"));
    }

    private boolean isBillPayment(String desc) {
        return desc.contains("PAGAMENTO FATURA") ||
                desc.contains("PAG FATURA") ||
                desc.contains("PGTO FATURA") ||
                desc.contains("DEBITO AUTOMATICO FATURA");
    }

    private boolean isInvestmentApplication(String desc) {
        return desc.contains("INVEST") || desc.contains("APLIC") ||
                desc.contains("CORRETORA") || desc.contains("CDB") ||
                desc.contains("COFRINHO") || desc.contains("TESOURO") ||
                desc.contains("POUPANCA") || desc.contains("B3");
    }
}