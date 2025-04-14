package com.example.flight_service.config;

import com.example.flight_service.filter.JwtFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtFilter jwtFilter;

    // Configure security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        try {
            http
                    .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS with custom config
                    .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/flights/add", "/flights/update/**", "/flights/delete/**").hasRole("ADMIN") // Admin-only endpoints
                            .requestMatchers("/flights/**", "/check-availability", "/book-seats", "/cancel-seat").hasRole("PASSENGER") // Passenger-only endpoints
                            .anyRequest().authenticated() // All other requests must be authenticated
                    )
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session management
                    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // Add JWT filter before default auth filter
                    .formLogin(form -> form.disable()) // Disable form login
                    .httpBasic(httpBasic -> httpBasic.disable()); // Disable basic HTTP auth

            return http.build();
        } catch (Exception e) {
            logger.error("Failed to configure security filter chain", e);
            throw e;
        }
    }

    // Configure allowed CORS settings
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        try {
            configuration.setAllowedOrigins(List.of("http://localhost:4201")); // Allow frontend origin
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // Allow HTTP methods
            configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Allow headers
            configuration.setAllowCredentials(true); // Allow credentials

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        } catch (Exception e) {
            logger.error("Error configuring CORS settings", e);
            throw new RuntimeException("Failed to configure CORS", e);
        }
    }

    // Relax the HTTP firewall to allow encoded characters
    @Bean
    public HttpFirewall relaxedHttpFirewall() {
        try {
            StrictHttpFirewall firewall = new StrictHttpFirewall();
            firewall.setAllowUrlEncodedSlash(true);
            firewall.setAllowSemicolon(true);
            firewall.setAllowUrlEncodedPercent(true);
            return firewall;
        } catch (Exception e) {
            logger.error("Failed to configure relaxed HTTP firewall", e);
            throw new RuntimeException("Failed to configure HTTP firewall", e);
        }
    }

    // Dummy user details service (not used in this service)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> null;
    }
}
