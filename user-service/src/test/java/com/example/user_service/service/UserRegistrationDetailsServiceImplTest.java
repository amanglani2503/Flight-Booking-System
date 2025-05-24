package com.example.user_service.service;

import com.example.user_service.model.Role;
import com.example.user_service.model.UserRegistration;
import com.example.user_service.model.UserPrincipal;
import com.example.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRegistrationDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_ShouldReturnUserPrincipal_WhenUserExists() {
        // Arrange
        String email = "alice@example.com";
        UserRegistration userRegistration = new UserRegistration();
        userRegistration.setId(1L);
        userRegistration.setName("Alice");
        userRegistration.setEmail(email);
        userRegistration.setPassword("encodedPass");
        userRegistration.setRole(Role.ADMIN);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userRegistration));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof UserPrincipal);
        assertEquals(email, result.getUsername());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(email);
        });
        verify(userRepository, times(1)).findByEmail(email);
    }
}
