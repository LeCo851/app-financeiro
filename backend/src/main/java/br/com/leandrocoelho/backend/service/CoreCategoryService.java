package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import br.com.leandrocoelho.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoreCategoryService {

    private final CategoryRepository categoryRepository;
    @Transactional
    public Category findOrCreateCategory(String categoryName, User user, TransactionType type){
        if(categoryName == null || categoryName.isBlank()){
            return null;
        }

        Optional<Category> existing = categoryRepository.findByUser_IdAndNameIgnoreCase(user.getId(), categoryName);

        if(existing.isPresent()){
            return existing.get();
        }

        Category newCategory = Category.builder()
                .user(user)
                .name(categoryName)
                .type(type)
                .icon("pi-tag")
                .color(generateRandomColor())
                .build();
        return categoryRepository.save(newCategory);
    }

    private String generateRandomColor() {
        // Gera cores mais escuras/sóbrias para contrastar com texto branco
        String[] colors = {"#3b82f6", "#ef4444", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899", "#6366f1", "#14b8a6"};
        int idx = (int) (Math.random() * colors.length);
        return colors[idx];
    }

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
    }

    @Transactional
    public Category resolveCategory(String pluggyCategoryName, User user, TransactionType type) {
        if (pluggyCategoryName == null) return null;

        String translatedName = CATEGORY_TRANSLATIONS.getOrDefault(pluggyCategoryName, pluggyCategoryName);

        // Tenta buscar no banco (cache de primeiro nível do Hibernate ajuda aqui)
        return categoryRepository.findByNameAndUser_Id(translatedName, user.getId())
                .orElseGet(() -> createCategory(translatedName, user, type));
    }

    private Category createCategory(String name, User user, TransactionType type) {
        return categoryRepository.save(Category.builder()
                .user(user)
                .name(name)
                .type(type)
                .icon("pi pi-tag")
                .color("#64748b")
                .build());
    }
}
