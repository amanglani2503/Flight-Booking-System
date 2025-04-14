package com.example.flight_service.filter;

import com.example.flight_service.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JWTService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testValidToken_setsAuthentication() throws Exception {
        String token = "valid.token";
        String username = "test@example.com";
        String role = "ADMIN";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.extractRole(token)).thenReturn(role);
        when(jwtService.validateToken(eq(token), any())).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(username, auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_ADMIN")));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testInvalidToken_doesNotAuthenticate() throws Exception {
        String token = "invalid.token";
        String username = "test@example.com";
        String role = "PASSENGER";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.extractRole(token)).thenReturn(role);
        when(jwtService.validateToken(eq(token), any())).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testNoAuthorizationHeader_doesNothing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testExceptionDuringParsing_logsErrorAndContinues() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(jwtService.extractUsername(any())).thenThrow(new RuntimeException("JWT parse failed"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
