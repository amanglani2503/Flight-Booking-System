package com.example.user_service.service;

import com.example.user_service.customexceptions.TokenNotFoundException;
import com.example.user_service.customexceptions.UserNotFoundException;
import com.example.user_service.model.UserRegistration;
import com.example.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public UserRegistration getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
    }

    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        logger.debug("Extracting token from Authorization header");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.debug("JWT token extracted successfully");
            return token;
        }

        logger.warn("Authorization token is missing or malformed");
        throw new TokenNotFoundException("Authorization token is missing or malformed");
    }

    public UserRegistration updateUser(Integer userId, UserRegistration updatedUserRegistration) {
        logger.info("Attempting to update user with ID: {}", userId);

        return userRepository.findById(userId).map(user -> {
            if (updatedUserRegistration.getName() != null) {
                logger.debug("Updating name for user ID {}: {}", userId, updatedUserRegistration.getName());
                user.setName(updatedUserRegistration.getName());
            }

            if (updatedUserRegistration.getEmail() != null) {
                logger.debug("Updating email for user ID {}: {}", userId, updatedUserRegistration.getEmail());
                user.setEmail(updatedUserRegistration.getEmail());
            }

            if (updatedUserRegistration.getPassword() != null) {
                logger.debug("Updating password for user ID {}", userId);
                String encodedPassword = new BCryptPasswordEncoder().encode(updatedUserRegistration.getPassword());
                user.setPassword(encodedPassword);
            }

            UserRegistration savedUser = userRepository.save(user);
            logger.info("User updated successfully with ID: {}", userId);
            return savedUser;
        }).orElseThrow(() -> {
            logger.warn("User not found with ID: {}", userId);
            return new UserNotFoundException("User not found with ID: " + userId);
        });
    }
}
