package br.com.leandrocoelho.backend.service.ai;

import br.com.leandrocoelho.backend.dto.response.CategoryExpenseDto;
import br.com.leandrocoelho.backend.dto.response.DashboardSummaryDto;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.repository.InvestmentRepository;
import br.com.leandrocoelho.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialAdvisorContextService {

    private final DashboardService dashboardService;
    private final InvestmentRepository investmentRepository;

    public String buildAdvisorContext(UUID userId) {
        // 1. Define a base de análise (Mês Anterior Completo)
        LocalDate lastMonth = LocalDate.now().minusMonths(1);

        // 2. Busca TODOS os dados já processados pelo DashboardService
        DashboardSummaryDto dashboard = dashboardService.getDashboardSummary(
                userId,
                lastMonth.getYear(),
                lastMonth.getMonthValue()
        );

        // 3. Busca Investimentos (Separado pois é Estoque de Riqueza, não Fluxo Mensal)
        BigDecimal totalInvestments = investmentRepository.sumTotalActiveInvestments(userId);
        if (totalInvestments == null) totalInvestments = BigDecimal.ZERO;

        // 4. Calcula Patrimônio Total (Liquidez + Investimentos)
        BigDecimal totalNetWorth = dashboard.getCurrentBalance().add(totalInvestments);

        // 5. Monta a Narrativa com StringBuilder (Mais eficiente para listas)
        StringBuilder sb = new StringBuilder();

        sb.append("--- RAIO-X FINANCEIRO (Baseado no mês anterior) ---\n\n");

        // [BLOCO 1] Saúde Financeira Macro
        sb.append("[MACROECONOMIA PESSOAL]\n");
        sb.append(String.format("- Renda Média: %s\n", formatMoney(dashboard.getAverageIncome())));
        sb.append(String.format("- Gastos Fixos Comprometidos: %s\n", formatMoney(dashboard.getTotalFixedExpense())));
        sb.append(String.format("- Comprometimento da Renda: %.1f%%\n", dashboard.getCommitmentPct()));
        sb.append(String.format("- Salário livre (Safe-to-Spend): %s\n", formatMoney(dashboard.getSafeToExpend())));
        sb.append(String.format("- Saldo em Conta (Liquidez): %s\n", formatMoney(dashboard.getCurrentBalance())));
        sb.append(String.format("- Investimentos (Reserva): %s\n", formatMoney(totalInvestments)));
        sb.append(String.format("- Patrimônio Total: %s\n\n", formatMoney(totalNetWorth)));

        // [BLOCO 2] Onde o dinheiro está indo (Top Categorias)
        sb.append("[ONDE O DINHEIRO FOI (Vilões do Orçamento)]\n");
        List<CategoryExpenseDto> topCategories = dashboard.getTopExpenseCategories();

        if (topCategories == null || topCategories.isEmpty()) {
            sb.append("- Sem dados de gastos significativos no período.\n");
        } else {
            for (CategoryExpenseDto cat : topCategories) {
                sb.append(String.format("- %s: %s\n", cat.name(), formatMoney(cat.amount())));
            }
        }
        sb.append("\n");

        // [BLOCO 3] Comportamento Recente (Últimas Transações)
        sb.append("[ÚLTIMAS MOVIMENTAÇÕES (Contexto Imediato)]\n");
        List<Transaction> recent = dashboard.getRecentTransactions();

        if (recent == null || recent.isEmpty()) {
            sb.append("- Nenhuma movimentação recente.\n");
        } else {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
            for (Transaction t : recent) {
                // Ex: - 09/02 | Uber | -R$ 25.00
                sb.append(String.format("- %s | %s | %s\n",
                        t.getDate().format(dtf),
                        t.getDescription(),
                        formatMoney(t.getAmount())));
            }
        }

        // [BLOCO 4] DIRETRIZES ESTRATÉGICAS (O Cérebro do Advisor)
        sb.append("\n[DIRETRIZES PARA O CONSULTOR IA]\n");

        // AQUI ESTÁ A REGRA 50/30/20 INJETADA DINAMICAMENTE
        double commitment = dashboard.getCommitmentPct();
        sb.append(String.format("1. REGRA 50/30/20: Atualmente os Gastos Fixos (Necessidades) consomem %.1f%% da renda (O ideal é 50%%).\n", commitment));

        if (commitment > 60.0) {
            sb.append("   -> SITUAÇÃO DE ALERTA: O usuário está gastando muito com fixos. Recomende cortar custos antes de qualquer compra supérflua.\n");
        } else {
            sb.append("   -> SITUAÇÃO CONTROLADA: O usuário tem espaço no orçamento. Incentive usar a 'Margem Livre' para atingir 20% em Investimentos e 30% em Desejos.\n");
        }

        sb.append("2. ANÁLISE DE CATEGORIA: Verifique o bloco 'TOP CATEGORIAS'. Se 'Restaurantes' ou 'Lazer' forem os maiores gastos, alerte sobre o desequilíbrio nos 30% de Desejos.\n");
        sb.append("3. PROTEÇÃO PATRIMONIAL: O usuário tem ").append(formatMoney(totalInvestments)).append(" investidos. Desencoraje o uso desse valor para compras de consumo (perda de juros compostos).\n");
        sb.append("4. CONTEXTO IMEDIATO: Se houver compras recentes grandes (vide 'Últimas Movimentações'), sugira cautela nos próximos dias.");

        return sb.toString();
    }

    private String formatMoney(BigDecimal value) {
        return value != null ? "R$ " + value.setScale(2, RoundingMode.HALF_UP) : "R$ 0.00";
    }
}