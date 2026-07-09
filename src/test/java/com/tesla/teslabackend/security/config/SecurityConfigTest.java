package com.tesla.teslabackend.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitario de la configuración de seguridad: valida el parseo de orígenes
 * CORS (separados por coma, con espacios y tokens vacíos) sin arrancar contexto
 * de Spring ni tocar la red (el JwtDecoder real no se instancia). Patrón AAA.
 */
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
