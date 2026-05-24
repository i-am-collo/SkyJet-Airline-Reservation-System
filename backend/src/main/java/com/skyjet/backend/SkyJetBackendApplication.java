package com.skyjet.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SkyJet Backend API - Spring Boot Application
 * Serves REST endpoints for the JavaFX frontend
 */
@SpringBootApplication
public class SkyJetBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkyJetBackendApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
