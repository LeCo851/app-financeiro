package br.com.leandrocoelho.backend.repository;

import br.com.leandrocoelho.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByUser_IdAndNameIgnoreCase(UUID userId, String name);
    List<Category> findByUser_Id(UUID userId);

    Optional<Category>findByNameAndUser_Id(String name,UUID userId);

    List<Category>findByUser_IdOrderByNameAsc(UUID userId);
}