package com.tesla.teslabackend.common.logging;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    /**
     * Ejecuta el filtro capturando el valor presente en el MDC mientras corre la
     * cadena, para verificar la correlacion sin depender del estado ya limpiado.
     */
    private String[] runAndCapture(String incomingHeader) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).thenReturn(incomingHeader);

        String[] mdcDuringChain = new String[1];
        doAnswer(invocation -> {
            mdcDuringChain[0] = MDC.get(RequestIdFilter.REQUEST_ID_KEY);
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq(RequestIdFilter.REQUEST_ID_HEADER), headerValue.capture());
        return new String[] { mdcDuringChain[0], headerValue.getValue() };
    }

    @Test
    void doFilter_generaRequestIdYloPublicaEnMdcyCabecera_cuandoNoVieneEnLaPeticion() throws Exception {
        // Arrange & Act
        String[] result = runAndCapture(null);
        String mdcValue = result[0];
        String headerValue = result[1];

        // Assert
        assertThat(mdcValue).isNotBlank();
        assertThat(headerValue).isEqualTo(mdcValue);
        assertThat(MDC.get(RequestIdFilter.REQUEST_ID_KEY)).isNull(); // se limpio tras la cadena
    }

    @Test
    void doFilter_reutilizaElRequestIdEntrante_cuandoVieneEnLaCabecera() throws Exception {
        // Arrange
        String incoming = "abc-123-correlation";

        // Act
        String[] result = runAndCapture(incoming);

        // Assert
        assertThat(result[0]).isEqualTo(incoming); // MDC durante la cadena
        assertThat(result[1]).isEqualTo(incoming); // cabecera de respuesta
    }
}
