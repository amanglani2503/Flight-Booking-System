package com.example.user_service.controller;

import com.example.user_service.model.UserRegistration;
import com.example.user_service.service.JWTService;
import com.example.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/profile")
    public ResponseEntity<UserRegistration> getUserProfile(HttpServletRequest request) {
        logger.info("Received request to fetch user profile");

        String token = userService.extractToken(request);
        logger.debug("Token extracted from request");

        String email = jwtService.extractUsername(token);
        logger.info("Extracted email from token: {}", email);

        UserRegistration userRegistration = userService.getUserByEmail(email);
        logger.info("Successfully fetched profile for email: {}", email);

        return ResponseEntity.ok(userRegistration);
    }

    @PutMapping("/update-profile/{id}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<UserRegistration> updateUserProfile(@PathVariable Integer id, @Valid @RequestBody UserRegistration updatedUserRegistration) {
        logger.info("Received request to update profile for user ID: {}", id);

        UserRegistration userRegistration = userService.updateUser(id, updatedUserRegistration);
        logger.info("Profile updated successfully for user ID: {}", id);

        return ResponseEntity.ok(userRegistration);
    }
}
