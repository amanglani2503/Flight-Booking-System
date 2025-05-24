package com.example.user_service.controller;

import com.example.user_service.model.Role;
import com.example.user_service.model.UserLogin;
import com.example.user_service.model.UserRegistration;
import com.example.user_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistration userRegistration) {
        userRegistration.setRole(Role.valueOf("PASSENGER"));
        UserRegistration registeredUserRegistration = authService.register(userRegistration);
        return ResponseEntity.ok(registeredUserRegistration);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLogin userLogin) {
        String token = authService.login(userLogin);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
