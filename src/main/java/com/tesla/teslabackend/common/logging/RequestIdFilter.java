package com.tesla.teslabackend.common.logging;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Asigna un identificador de correlacion a cada request y lo publica en el
 * {@link MDC}, de modo que aparezca como campo en los logs JSON (ECS) y pueda
 * cruzarse en CloudWatch Logs Insights. Tambien lo devuelve en la cabecera
 * {@code X-Request-Id} para correlacionar la respuesta del cliente con los logs.
 *
 * <p>Se ejecuta con la maxima precedencia para que el identificador este
 * disponible antes de la cadena de seguridad y del resto de filtros.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID_KEY);
        }
    }

    /**
     * Reutiliza el {@code X-Request-Id} entrante si viene informado (p. ej. desde
     * CloudFront/ALB); en caso contrario genera un UUID nuevo.
     */
    private String resolveRequestId(HttpServletRequest request) {
        String incoming = request.getHeader(REQUEST_ID_HEADER);
        return StringUtils.hasText(incoming) ? incoming : UUID.randomUUID().toString();
    }
}
