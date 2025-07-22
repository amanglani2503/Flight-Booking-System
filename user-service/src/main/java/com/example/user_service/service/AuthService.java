package com.example.user_service.service;

import com.example.user_service.customexceptions.InvalidCredentialsException;
import com.example.user_service.customexceptions.UserAlreadyExistsException;
import com.example.user_service.customexceptions.UserNotFoundException;
import com.example.user_service.model.UserLogin;
import com.example.user_service.model.UserRegistration;
import com.example.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserRegistration register(UserRegistration userRegistration) {
        logger.info("Attempting to register user with email: {}", userRegistration.getEmail());

        Optional<UserRegistration> userInDB = userRepository.findByEmail(userRegistration.getEmail());
        if (userInDB.isPresent()) {
            logger.warn("User already exists with email: {}", userRegistration.getEmail());
            throw new UserAlreadyExistsException("User already exists!");
        }

        userRegistration.setPassword(encoder.encode(userRegistration.getPassword()));
        UserRegistration savedUser = userRepository.save(userRegistration);

        logger.info("User registered successfully with email: {}", savedUser.getEmail());
        return savedUser;
    }

    public String login(UserLogin userLogin) {
        logger.info("Login attempt for email: {}", userLogin.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword()));

        if (authentication.isAuthenticated()) {
            logger.info("Authentication successful for email: {}", userLogin.getEmail());

            return userRepository.findByEmail(userLogin.getEmail())
                    .map(user -> {
                        String token = jwtService.generateToken(userLogin.getEmail(), user.getRole());
                        logger.info("JWT token generated for email: {}", userLogin.getEmail());
                        return token;
                    })
                    .orElseThrow(() -> {
                        logger.error("User not found in DB after authentication: {}", userLogin.getEmail());
                        return new UserNotFoundException("User not found after successful authentication");
                    });
        }

        logger.warn("Authentication failed for email: {}", userLogin.getEmail());
        throw new InvalidCredentialsException("Invalid credentials");
    }
}
