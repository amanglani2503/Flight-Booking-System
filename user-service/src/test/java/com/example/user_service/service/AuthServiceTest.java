package com.example.user_service.service;

import com.example.user_service.model.Role;
import com.example.user_service.model.UserLogin;
import com.example.user_service.model.UserRegistration;
import com.example.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_ShouldEncodePasswordAndSaveUser() {
        // Arrange
        UserRegistration userRegistration = new UserRegistration();
        userRegistration.setName("Alice");
        userRegistration.setEmail("alice@example.com");
        userRegistration.setPassword("password123");
        userRegistration.setRole(Role.PASSENGER);

        when(userRepository.save(any(UserRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserRegistration savedUserRegistration = authService.register(userRegistration);

        // Assert
        assertNotNull(savedUserRegistration);
        assertNotEquals("password123", savedUserRegistration.getPassword()); // Password should be encoded
        verify(userRepository, times(1)).save(any(UserRegistration.class));
    }

    @Test
    void testLogin_ShouldReturnJwtTokenOnSuccessfulAuth() {
        // Arrange
        UserLogin userLogin = new UserLogin();
        userLogin.setEmail("bob@example.com");
        userLogin.setPassword("securePass");

        UserRegistration dbUser = new UserRegistration();
        dbUser.setEmail("bob@example.com");
        dbUser.setRole(Role.ADMIN);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(dbUser));
        when(jwtService.generateToken("bob@example.com", Role.ADMIN)).thenReturn("mock-jwt-token");

        // Act
        String token = authService.login(userLogin);

        // Assert
        assertEquals("mock-jwt-token", token);
        verify(jwtService, times(1)).generateToken("bob@example.com", Role.ADMIN);
    }


    @Test
    void testLogin_ShouldReturnNullWhenAuthenticationFails() {
        // Arrange
        UserLogin userLogin = new UserLogin();

        userLogin.setEmail("fake@example.com");
        userLogin.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // Act
        String token = authService.login(userLogin);

        // Assert
        assertNull(token);
    }
}