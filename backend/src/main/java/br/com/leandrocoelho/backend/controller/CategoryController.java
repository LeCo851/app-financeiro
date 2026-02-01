package br.com.leandrocoelho.backend.controller;

import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<Category>> listMyCategories(@AuthenticationPrincipal Jwt jwt){

        UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));

        List<Category> categories = categoryRepository.findByUser_IdOrderByNameAsc(userId);
        return ResponseEntity.ok(categories);
    }
}
