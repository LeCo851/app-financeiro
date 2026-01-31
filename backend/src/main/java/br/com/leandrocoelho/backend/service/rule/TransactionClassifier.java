package br.com.leandrocoelho.backend.service.rule;

import br.com.leandrocoelho.backend.model.enums.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class TransactionClassifier {

    public TransactionType classify(String pluggyType, String description) {
        // Normaliza para evitar problemas de Case
        boolean isCredit = "CREDIT".equalsIgnoreCase(pluggyType);
        String desc = description != null ? description.toUpperCase().trim() : "";

        // =============================================================
        // CENÁRIO A: DINHEIRO ENTRANDO (CREDIT)
        // =============================================================
        if (isCredit) {
            // Pagamento de Fatura entrando no limite do cartão
            if (isPaymentReceived(desc)) return TransactionType.TRANSFER;

            // Resgate de Investimento (Dinheiro voltando pra conta)
            if (isInvestmentRedemption(desc)) return TransactionType.TRANSFER;

            return TransactionType.INCOME; // Padrão: Receita
        }

        // =============================================================
        // CENÁRIO B: DINHEIRO SAINDO (DEBIT)
        // =============================================================
        else {
            // Dinheiro saindo da conta para pagar a fatura
            if (isBillPayment(desc)) return TransactionType.TRANSFER;

            // Dinheiro saindo para aplicação financeira
            if (isInvestmentApplication(desc)) return TransactionType.INVESTMENT;

            return TransactionType.EXPENSE; // Padrão: Despesa
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
        return desc.contains("RESGATE");
    }

    private boolean isBillPayment(String desc) {
        return desc.contains("PAGAMENTO FATURA") ||
                desc.contains("PAG FATURA") ||
                desc.contains("PGTO FATURA") ||
                desc.contains("DEBITO AUTOMATICO FATURA")||
                desc.contains("INT ITAU MULT")||
                desc.contains("INT CARTAO")||
                desc.contains("FATURA");
    }

    private boolean isInvestmentApplication(String desc) {
        return desc.contains("INVEST") || desc.contains("APLIC") ||
                desc.contains("CORRETORA") || desc.contains("CDB") ||
                desc.contains("COFRINHO") || desc.contains("TESOURO") ||
                desc.contains("POUPANCA") || desc.contains("B3");
    }
}