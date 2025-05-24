package com.example.user_service.service;

import com.example.user_service.customexceptions.TokenNotFoundException;
import com.example.user_service.customexceptions.UserNotFoundException;
import com.example.user_service.model.UserRegistration;
import com.example.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserRegistration getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new TokenNotFoundException("Authorization token is missing or malformed");
    }

    public UserRegistration updateUser(Integer userId, UserRegistration updatedUserRegistration) {
        return userRepository.findById(userId).map(user -> {
            if (updatedUserRegistration.getName() != null) {
                user.setName(updatedUserRegistration.getName());
            }
            if (updatedUserRegistration.getEmail() != null) {
                user.setEmail(updatedUserRegistration.getEmail());
            }
            if (updatedUserRegistration.getPassword() != null) {
                String encodedPassword = new BCryptPasswordEncoder().encode(updatedUserRegistration.getPassword());
                user.setPassword(encodedPassword);
            }
            return userRepository.save(user);
        }).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }
}
