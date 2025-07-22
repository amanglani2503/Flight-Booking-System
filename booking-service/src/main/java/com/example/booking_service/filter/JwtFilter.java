package com.example.booking_service.filter;

import com.example.booking_service.service.JWTService;
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

            if (authHeader == null) {
                logger.debug("Authorization header is missing");
            } else if (!authHeader.startsWith("Bearer ")) {
                logger.warn("Authorization header does not start with 'Bearer ': {}", authHeader);
            } else {
                token = authHeader.substring(7).trim();
                username = jwtService.extractUsername(token);
                role = jwtService.extractRole(token);
                logger.debug("Token extracted for user: {}", username);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String formattedRole = (role != null && role.startsWith("ROLE_")) ? role : "ROLE_" + role;

                UserDetails userDetails = new User(
                        username,
                        "",
                        List.of(new SimpleGrantedAuthority(formattedRole))
                );

                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication set for user: {}", username);
                } else {
                    logger.warn("Invalid JWT token for user: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("Exception occurred in JWT filter: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
