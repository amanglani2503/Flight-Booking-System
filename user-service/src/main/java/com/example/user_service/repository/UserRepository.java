package com.example.user_service.repository;

import com.example.user_service.model.UserRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserRegistration, Integer> {
    Optional<UserRegistration> findByEmail(String email);
}
