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


//     Extracts the role claim from a JWT token
    public String extractRole(String token) {
        try {
            return extractAllClaims(token).get("role", String.class);
        } catch (Exception e) {
            logger.error("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }


//    Extracts the username (subject) from the token.
     public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


//     Generic method to extract a specific claim using a resolver function.
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

//    Parses and validates the JWT, returning all claims.
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("Token is expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

//     Decodes the Base64-encoded secret key into a SecretKey object.
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


//    Validates a token by checking the username and expiration.
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }


//     Returns whether the token is expired.
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


//     Extracts the expiration time from the token.
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
