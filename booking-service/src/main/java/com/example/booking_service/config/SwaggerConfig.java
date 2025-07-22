package com.example.booking_service.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI flightSearchOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Flight Service API")
                        .description("This service allows admins to add flights and it's details")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Amber Manglani")
                                .email("amanglani2503@gmail.com")
                                .url("http://noDomainNow.com")
                        )
                )
                .externalDocs(new ExternalDocumentation()
                        .description("Project Github")
                        .url("https://github.com/amanglani2503/FlightService-FlightBookingSystem")
                );
    }
}