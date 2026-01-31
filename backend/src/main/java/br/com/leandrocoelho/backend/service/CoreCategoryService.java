package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import br.com.leandrocoelho.backend.repository.CategoryRepository;
import br.com.leandrocoelho.backend.service.ai.AiCategorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreCategoryService {

    private final CategoryRepository categoryRepository;
    private final AiCategorizationService aiService;

    private static final Map<String, String> CATEGORY_TRANSLATIONS = new HashMap<>();
    static {
        CATEGORY_TRANSLATIONS.put("Food & Drink", "Alimentação");
        CATEGORY_TRANSLATIONS.put("Shopping", "Compras");
        CATEGORY_TRANSLATIONS.put("Transport", "Transporte");
        CATEGORY_TRANSLATIONS.put("Bills & Utilities", "Contas e Serviços");
        CATEGORY_TRANSLATIONS.put("Entertainment", "Lazer");
        CATEGORY_TRANSLATIONS.put("Health", "Saúde");
        CATEGORY_TRANSLATIONS.put("Travel", "Viagem");
        CATEGORY_TRANSLATIONS.put("Income", "Receita");
        CATEGORY_TRANSLATIONS.put("Transfer", "Transferência");
        CATEGORY_TRANSLATIONS.put("Investments", "Investimentos");
        CATEGORY_TRANSLATIONS.put("Taxes", "Impostos");
        CATEGORY_TRANSLATIONS.put("Loans", "Empréstimos");
        CATEGORY_TRANSLATIONS.put("Education", "Educação");
        CATEGORY_TRANSLATIONS.put("Family & Personal", "Família e Pessoal");
        CATEGORY_TRANSLATIONS.put("Withdrawal", "Saque");
        CATEGORY_TRANSLATIONS.put("Services", "Serviços");
        CATEGORY_TRANSLATIONS.put("Credit Card", "Transferência"); // Pagamento de fatura geralmente vem assim
    }

    /**
     * Método principal de resolução de categoria.
     * Fluxo: Mapa Estático -> IA (Groq) -> Banco de Dados
     */
    @Transactional
    public Category resolveCategory(String description, String merchantName, String pluggyCategoryName, User user, TransactionType type) {
        String categoryName = null;

        // 1. TENTATIVA RÁPIDA: Mapa de Tradução (Se a Pluggy já mandou algo útil)
        if (pluggyCategoryName != null) {
            categoryName = CATEGORY_TRANSLATIONS.get(pluggyCategoryName);
        }

        // 2. TENTATIVA INTELIGENTE: Se o mapa falhou ou não tem categoria, chama a IA
        if (categoryName == null) {
            log.info("Categoria Pluggy ausente ou desconhecida. Acionando IA para: '{}' | Estabelecimento: '{}'", description, merchantName);
            categoryName = aiService.categorize(description, merchantName);
        }

        // 3. PERSISTÊNCIA: Busca no banco ou cria nova (Cache)
        return findOrCreateInDb(categoryName, user, type);
    }

    private Category findOrCreateInDb(String name, User user, TransactionType type) {
        if (name == null || name.isBlank()) return null;

        // Tenta achar ignorando Case (Alimentação == alimentação)
        return categoryRepository.findByUser_IdAndNameIgnoreCase(user.getId(), name)
                .orElseGet(() -> createCategory(name, user, type));
    }

    private Category createCategory(String name, User user, TransactionType type) {
        return categoryRepository.save(Category.builder()
                .user(user)
                .name(name)
                .type(type)
                .icon(resolveIcon(name)) // Ícone dinâmico
                .color(generateRandomColor()) // Cor dinâmica (usei sua lógica aleatória que é boa)
                .build());
    }

    private String resolveIcon(String name) {
        if (name == null) return "pi pi-tag";
        String n = name.toLowerCase();

        if (n.contains("aliment")) return "pi pi-shopping-cart";
        if (n.contains("transporte") || n.contains("carro") || n.contains("uber")) return "pi pi-car";
        if (n.contains("lazer") || n.contains("cinema")) return "pi pi-ticket";
        if (n.contains("saúde") || n.contains("farma")) return "pi pi-heart";
        if (n.contains("invest")) return "pi pi-chart-line";
        if (n.contains("casa") || n.contains("moradia")) return "pi pi-home";
        if (n.contains("contas") || n.contains("serviços")) return "pi pi-bolt";
        if (n.contains("educa")) return "pi pi-book";

        return "pi pi-tag";
    }

    private String generateRandomColor() {
        String[] colors = {"#3b82f6", "#ef4444", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899", "#6366f1", "#14b8a6", "#64748b"};
        int idx = (int) (Math.random() * colors.length);
        return colors[idx];
    }
}