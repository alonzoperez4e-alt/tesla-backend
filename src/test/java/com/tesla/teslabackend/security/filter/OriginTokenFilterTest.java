package com.tesla.teslabackend.security.filter;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OriginTokenFilterTest {

    private static final String TOKEN = "token-secreto-de-cloudfront";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StringWriter cuerpoRespuesta = new StringWriter();

    private HttpServletRequest peticion(String uri, String cabecera) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        lenientHeader(request, cabecera);
        return request;
    }

    private void lenientHeader(HttpServletRequest request, String cabecera) {
        when(request.getHeader(OriginTokenFilter.ORIGIN_TOKEN_HEADER)).thenReturn(cabecera);
    }

    private HttpServletResponse respuesta() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(cuerpoRespuesta));
        return response;
    }

    @Test
    void doFilter_dejaPasar_cuandoElTokenCoincide() throws Exception {
        // Arrange
        OriginTokenFilter filter = new OriginTokenFilter(TOKEN, objectMapper);
        HttpServletRequest request = peticion("/api/v1/cursos", TOKEN);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(403);
    }

    @Test
    void doFilter_devuelve403_cuandoFaltaLaCabecera() throws Exception {
        // Arrange
        OriginTokenFilter filter = new OriginTokenFilter(TOKEN, objectMapper);
        HttpServletRequest request = peticion("/api/v1/cursos", null);
        when(request.getMethod()).thenReturn("GET");
        HttpServletResponse response = respuesta();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert: se corta la cadena y el cuerpo usa el formato de error estandar.
        verify(chain, never()).doFilter(request, response);
        verify(response).setStatus(403);
        assertThat(cuerpoRespuesta.toString()).contains("ORIGEN_NO_AUTORIZADO");
    }

    @Test
    void doFilter_devuelve403_cuandoElTokenNoCoincide() throws Exception {
        // Arrange
        OriginTokenFilter filter = new OriginTokenFilter(TOKEN, objectMapper);
        HttpServletRequest request = peticion("/api/v1/cursos", "token-equivocado");
        when(request.getMethod()).thenReturn("POST");
        HttpServletResponse response = respuesta();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain, never()).doFilter(request, response);
        verify(response).setStatus(403);
    }

    @Test
    void doFilter_dejaPasarActuatorSinCabecera_paraNoRomperElHealthcheck() throws Exception {
        // Arrange: el HEALTHCHECK del contenedor llama a localhost sin pasar por el CDN.
        OriginTokenFilter filter = new OriginTokenFilter(TOKEN, objectMapper);
        HttpServletRequest request = peticion("/actuator/health/liveness", null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(403);
    }

    @Test
    void doFilter_noValidaNada_cuandoElTokenNoEstaConfigurado() throws Exception {
        // Arrange: token vacio = filtro desactivado (solo posible fuera de prod).
        OriginTokenFilter filter = new OriginTokenFilter("", objectMapper);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(403);
    }
}
