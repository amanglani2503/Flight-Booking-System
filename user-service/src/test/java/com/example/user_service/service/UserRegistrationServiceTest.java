package com.example.user_service.service;

import com.example.user_service.model.Role;
import com.example.user_service.model.UserRegistration;
import com.example.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // getUserByEmail()

    @Test
    void testGetUserByEmail_ShouldReturnUser_WhenFound() {
        String email = "test@example.com";
        UserRegistration mockUserRegistration = new UserRegistration();
        mockUserRegistration.setId(1L);
        mockUserRegistration.setEmail(email);
        mockUserRegistration.setName("Test");
        mockUserRegistration.setRole(Role.PASSENGER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUserRegistration));

        UserRegistration result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testGetUserByEmail_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserByEmail("missing@example.com"));
    }

    // extractToken()

    @Test
    void testExtractToken_ShouldReturnToken_WhenHeaderIsValid() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer abc.def.ghi");

        String token = userService.extractToken(request);
        assertEquals("abc.def.ghi", token);
    }

    @Test
    void testExtractToken_ShouldReturnNull_WhenHeaderIsInvalid() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        assertNull(userService.extractToken(request));
    }

    @Test
    void testExtractToken_ShouldReturnNull_WhenHeaderIsMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        assertNull(userService.extractToken(request));
    }

    // updateUser()

    @Test
    void testUpdateUser_ShouldUpdateFields_WhenUserExists() {
        Integer userId = 1;
        UserRegistration existingUserRegistration = new UserRegistration();
        existingUserRegistration.setId(1L);
        existingUserRegistration.setEmail("old@example.com");
        existingUserRegistration.setName("Old Name");
        existingUserRegistration.setPassword("oldpass");
        existingUserRegistration.setRole(Role.ADMIN);

        UserRegistration updates = new UserRegistration();
        updates.setEmail("new@example.com");
        updates.setName("New Name");
        updates.setPassword("newpass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUserRegistration));
        when(userRepository.save(any(UserRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserRegistration updatedUserRegistration = userService.updateUser(userId, updates);

        assertEquals("New Name", updatedUserRegistration.getName());
        assertEquals("new@example.com", updatedUserRegistration.getEmail());
        assertNotEquals("newpass", updatedUserRegistration.getPassword()); // Should be encoded
    }

    @Test
    void testUpdateUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        UserRegistration dummyUpdate = new UserRegistration();
        dummyUpdate.setName("Update");

        assertThrows(RuntimeException.class, () -> userService.updateUser(99, dummyUpdate));
    }
}
