package com.tesla.teslabackend.security.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tesla.teslabackend.common.exception.ErrorResponse;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rechaza con 403 cualquier peticion que no traiga la cabecera secreta que
 * inyecta CloudFront ({@code X-Tesla-Origin-Token}).
 *
 * <p>No es una medida redundante sino el unico punto de aplicacion real: el HTTP
 * API se despliega con {@code disable_execute_api_endpoint = false} (no se puede
 * desactivar sin un dominio propio), asi que su URL {@code execute-api} es
 * publica y se salta CloudFront por completo. Sin esta comprobacion, cualquiera
 * podria alcanzar el backend evitando el CDN.</p>
 *
 * <p>Se ordena justo despues de {@link com.tesla.teslabackend.common.logging.RequestIdFilter}
 * para que los rechazos se registren ya con su {@code requestId}, y muy por
 * delante de la cadena de Spring Security para descartar el trafico ilegitimo
 * antes de procesar el JWT.</p>
 *
 * <p>El {@code ObjectMapper} inyectado es el de Jackson 3 ({@code tools.jackson}),
 * que es el que autoconfigura Spring Boot 4 como {@code JsonMapper}. Jackson 2
 * sigue en el classpath por dependencias transitivas, pero no tiene ningun bean
 * asociado.</p>
 *
 * <p>Si {@code app.security.origin-token} viene vacio el filtro se desactiva, lo
 * que permite levantar la app en local. En el perfil {@code prod} la propiedad se
 * declara sin valor por defecto, de modo que la ausencia de la variable
 * {@code ORIGIN_TOKEN} impide arrancar en vez de degradar la seguridad en
 * silencio.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class OriginTokenFilter extends OncePerRequestFilter {

    public static final String ORIGIN_TOKEN_HEADER = "X-Tesla-Origin-Token";

    private static final Logger logger = LoggerFactory.getLogger(OriginTokenFilter.class);

    /**
     * Rutas exentas: las sondas de Actuator las invoca el HEALTHCHECK del
     * contenedor contra localhost, sin pasar por CloudFront y por tanto sin
     * cabecera. Exigirsela dejaria la task en reinicio permanente.
     */
    private static final String ACTUATOR_PATH_PREFIX = "/actuator";

    private final byte[] expectedToken;
    private final boolean enabled;
    private final ObjectMapper objectMapper;

    public OriginTokenFilter(@Value("${app.security.origin-token:}") String expectedToken,
                             ObjectMapper objectMapper) {
        this.enabled = StringUtils.hasText(expectedToken);
        this.expectedToken = this.enabled ? expectedToken.getBytes(StandardCharsets.UTF_8) : new byte[0];
        this.objectMapper = objectMapper;

        if (!this.enabled) {
            logger.warn("app.security.origin-token esta vacio: la validacion de origen queda DESACTIVADA. "
                    + "Solo deberia ocurrir en desarrollo local.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        String path = request.getRequestURI();
        return path != null && path.startsWith(ACTUATOR_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (esTokenValido(request.getHeader(ORIGIN_TOKEN_HEADER))) {
            filterChain.doFilter(request, response);
            return;
        }

        // No se registra el valor recibido para no volcar secretos en los logs.
        logger.warn("Peticion rechazada por origen no autorizado: {} {}", request.getMethod(), request.getRequestURI());
        responderProhibido(response);
    }

    /** Comparacion en tiempo constante para no filtrar el token por temporizacion. */
    private boolean esTokenValido(String recibido) {
        if (!StringUtils.hasText(recibido)) {
            return false;
        }
        return MessageDigest.isEqual(recibido.getBytes(StandardCharsets.UTF_8), expectedToken);
    }

    private void responderProhibido(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(),
                new ErrorResponse("ORIGEN_NO_AUTORIZADO", "La peticion debe llegar a traves del CDN"));
    }
}
