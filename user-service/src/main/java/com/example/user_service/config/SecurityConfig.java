package com.example.user_service.config;

import com.example.user_service.filter.JwtFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String API_GATEWAY_HOST = "api-gateway";

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    // Defines security filter chain with authorization rules and JWT filter
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Initializing SecurityFilterChain");

        logger.debug("Disabling CSRF and enabling CORS");
        logger.debug("Setting authorization rules and adding JwtFilter before UsernamePasswordAuthenticationFilter");

        return http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> {
                    logger.debug("Configuring endpoint permissions");
                    auth.requestMatchers("/auth/register", "/auth/login", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
                    auth.requestMatchers("/users/**").hasRole("PASSENGER");
                    auth.requestMatchers(this::isRequestFromApiGateway).permitAll();
                    auth.anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .formLogin(form -> {
                    logger.debug("Disabling form login");
                    form.disable();
                })
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Checks if request originated from API Gateway based on custom header
    private boolean isRequestFromApiGateway(HttpServletRequest request) {
        String sourceHeader = request.getHeader("X-Source");
        logger.debug("Checking request source: X-Source = {}", sourceHeader);

        boolean isFromGateway = API_GATEWAY_HOST.equalsIgnoreCase(sourceHeader);
        logger.info("Request from API Gateway? {}", isFromGateway);

        return isFromGateway;
    }

    // Configures authentication provider using custom user details service and password encoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        logger.info("Creating DaoAuthenticationProvider with BCryptPasswordEncoder");

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);

        logger.debug("AuthenticationProvider configured with custom UserDetailsService and encoder");
        return provider;
    }

    // Provides authentication manager bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        logger.info("Creating AuthenticationManager bean from AuthenticationConfiguration");
        return config.getAuthenticationManager();
    }
}
