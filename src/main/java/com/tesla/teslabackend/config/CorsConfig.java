package com.tesla.teslabackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Permitimos el acceso a TODOS los endpoints (/**)
                registry.addMapping("/**")
                        // IMPORTANTE: Aquí van los orígenes permitidos.
                        // He incluido 8081 (tu caso), 5173 (Vite default) y 3000 (React default)
                        .allowedOrigins("http://localhost:8081", "http://localhost:5173", "http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}