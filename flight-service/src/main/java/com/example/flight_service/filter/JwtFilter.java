package com.example.flight_service.filter;

import com.example.flight_service.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;
            String role = null;

            // Extract token from Authorization header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
                username = jwtService.extractUsername(token);
                role = jwtService.extractRole(token);
                logger.debug("Token extracted for user: {}", username);
            }

            // Proceed only if the user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Ensure the role starts with "ROLE_"
                String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                // Create UserDetails manually based on token data
                UserDetails userDetails = new User(
                        username,
                        "", // No password needed since it's already authenticated via token
                        List.of(new SimpleGrantedAuthority(formattedRole))
                );

                // Validate token
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in the context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication set for user: {}", username);
                } else {
                    logger.warn("Invalid JWT token for user: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("Exception occurred in JWT filter: {}", e.getMessage(), e);
        }

        // Proceed with the filter chain
        filterChain.doFilter(request, response);
    }
}
