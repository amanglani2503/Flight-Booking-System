package com.example.user_service.filter;

import com.example.user_service.service.JWTService;
import com.example.user_service.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ApplicationContext context;

    // Intercepts requests to validate and set authentication based on JWT
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            logger.debug("Checking Authorization header");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                logger.debug("Extracted JWT token from header");
                username = jwtService.extractUsername(token);
                logger.debug("Email extracted from token: {}", username);
            } else {
                logger.debug("Authorization header missing or does not start with Bearer");
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Security context is unauthenticated. Proceeding with user details lookup.");

                UserDetails userDetails = context.getBean(UserDetailsServiceImpl.class).loadUserByUsername(username);
                logger.debug("UserDetails loaded for email: {}", username);

                if (jwtService.validateToken(token, userDetails)) {
                    logger.info("JWT token validated successfully for email: {}", username);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.debug("SecurityContext updated with authenticated token for email: {}", username);
                } else {
                    logger.warn("JWT token validation failed for email: {}", username);
                }
            } else if (username != null) {
                logger.debug("Security context already contains authentication for email: {}", username);
            }
        } catch (Exception e) {
            logger.error("Error occurred in JWT filter processing: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
