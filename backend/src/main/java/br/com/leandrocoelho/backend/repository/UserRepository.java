package br.com.leandrocoelho.backend.repository;

import br.com.leandrocoelho.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    
}
