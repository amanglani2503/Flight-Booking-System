package com.example.user_service.controller;

import com.example.user_service.model.Role;
import com.example.user_service.model.UserLogin;
import com.example.user_service.model.UserRegistration;
import com.example.user_service.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistration userRegistration) {
        logger.info("Received registration request for email: {}", userRegistration.getEmail());

        userRegistration.setRole(Role.valueOf("PASSENGER"));
        UserRegistration registeredUser = authService.register(userRegistration);

        logger.info("User registered successfully with email: {}", registeredUser.getEmail());
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLogin userLogin) {
        logger.info("Login attempt for email: {}", userLogin.getEmail());

        String token = authService.login(userLogin);

        logger.info("Login successful for email: {}", userLogin.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
