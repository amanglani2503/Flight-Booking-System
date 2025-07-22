package com.example.flight_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    // Extracts the role claim from a JWT token
    public String extractRole(String token) {
        logger.debug("Extracting role from token");
        try {
            String role = extractAllClaims(token).get("role", String.class);
            logger.debug("Role extracted: {}", role);
            return role;
        } catch (Exception e) {
            logger.error("Failed to extract role from token: {}", e.getMessage(), e);
            return null;
        }
    }

    // Extracts the username (subject) from the token.
    public String extractUsername(String token) {
        logger.debug("Extracting username from token");
        try {
            String username = extractClaim(token, Claims::getSubject);
            logger.debug("Username extracted: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Failed to extract username from token: {}", e.getMessage(), e);
            return null;
        }
    }

    // Generic method to extract a specific claim using a resolver function.
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    // Parses and validates the JWT, returning all claims.
    private Claims extractAllClaims(String token) {
        try {
            logger.debug("Parsing JWT token claims");
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("Token is expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid token");
        }
    }

    // Decodes the Base64-encoded secret key into a SecretKey object.
    private SecretKey getKey() {
        logger.debug("Decoding secret key for JWT parsing");
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Validates a token by checking the username and expiration.
    public boolean validateToken(String token, UserDetails userDetails) {
        logger.debug("Validating token for user: {}", userDetails.getUsername());
        try {
            final String username = extractUsername(token);
            boolean isValid = (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
            logger.debug("Token validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    // Returns whether the token is expired.
    private boolean isTokenExpired(String token) {
        boolean expired = extractExpiration(token).before(new Date());
        logger.debug("Token expired: {}", expired);
        return expired;
    }

    // Extracts the expiration time from the token.
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
