package com.tesla.teslabackend.common.exception;

import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.tesla.teslabackend.common.logging.RequestIdFilter;

/**
 * Respuesta de error estandar. El {@code requestId} correlaciona la respuesta con
 * los logs (mismo valor que la cabecera {@code X-Request-Id}); se omite del JSON
 * si no hay contexto de request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String error, String message, String requestId) {

    /**
     * Constructor de conveniencia: toma el {@code requestId} del MDC poblado por
     * {@link RequestIdFilter}, evitando propagarlo manualmente en cada handler.
     */
    public ErrorResponse(String error, String message) {
        this(error, message, MDC.get(RequestIdFilter.REQUEST_ID_KEY));
    }
}
