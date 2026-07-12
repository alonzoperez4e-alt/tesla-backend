package com.tesla.teslabackend.common.exception;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.tesla.teslabackend.common.logging.RequestIdFilter;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @AfterEach
    void limpiarMdc() {
        MDC.clear();
    }

    @Test
    void constructor_tomaElRequestIdDelMdc_cuandoHayContextoDeRequest() {
        // Arrange
        MDC.put(RequestIdFilter.REQUEST_ID_KEY, "req-42");

        // Act
        ErrorResponse response = new ErrorResponse("ALGUN_ERROR", "mensaje");

        // Assert
        assertThat(response.error()).isEqualTo("ALGUN_ERROR");
        assertThat(response.message()).isEqualTo("mensaje");
        assertThat(response.requestId()).isEqualTo("req-42");
    }

    @Test
    void constructor_dejaRequestIdNulo_cuandoNoHayContextoDeRequest() {
        // Arrange: sin requestId en el MDC

        // Act
        ErrorResponse response = new ErrorResponse("ALGUN_ERROR", "mensaje");

        // Assert
        assertThat(response.requestId()).isNull();
    }
}
