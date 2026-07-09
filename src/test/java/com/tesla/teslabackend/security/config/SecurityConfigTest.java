package com.tesla.teslabackend.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private static CorsConfiguration corsFor(String allowedOrigins) {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", allowedOrigins);
        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
        return source.getCorsConfigurations().get("/**");
    }

    @Test
    void corsConfigurationSource_recortaEspaciosYdescartaTokensVacios() {
        // Arrange
        String origins = "https://app.tesla.com, http://localhost:5173 ,";

        // Act
        CorsConfiguration cors = corsFor(origins);

        // Assert
        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins())
                .containsExactly("https://app.tesla.com", "http://localhost:5173");
        assertThat(cors.getAllowCredentials()).isTrue();
        assertThat(cors.getAllowedMethods())
                .contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }
}
