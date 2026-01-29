package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import br.com.leandrocoelho.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
