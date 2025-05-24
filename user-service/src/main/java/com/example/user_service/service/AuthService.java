package com.example.user_service.service;

import com.example.user_service.customexceptions.InvalidCredentialsException;
import com.example.user_service.customexceptions.UserAlreadyExistsException;
import com.example.user_service.customexceptions.UserNotFoundException;
import com.example.user_service.model.UserLogin;
import com.example.user_service.model.UserRegistration;
import com.example.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserRegistration register(UserRegistration userRegistration) {
        Optional<UserRegistration> userInDB = userRepository.findByEmail(userRegistration.getEmail());
        if (userInDB.isPresent()) {
            throw new UserAlreadyExistsException("User already exists!");
        }
        userRegistration.setPassword(encoder.encode(userRegistration.getPassword()));
        return userRepository.save(userRegistration);
    }

    public String login(UserLogin userLogin) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword()));

        if (authentication.isAuthenticated()) {
            return userRepository.findByEmail(userLogin.getEmail())
                    .map(u -> jwtService.generateToken(userLogin.getEmail(), u.getRole()))
                    .orElseThrow(() -> new UserNotFoundException("User not found after successful authentication"));
        }

        throw new InvalidCredentialsException("Invalid credentials");
    }
}

//<configuration>
//    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
//        <file>logs/userRegistration-service.log</file>
//        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
//            <fileNamePattern>logs/userRegistration-service-%d{yyyy-MM-dd}.log</fileNamePattern>
//            <maxHistory>30</maxHistory>
//        </rollingPolicy>
//        <encoder>
//            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
//        </encoder>
//    </appender>
//
//    <root level="INFO">
//        <appender-ref ref="FILE" />
//    </root>
//</configuration>
