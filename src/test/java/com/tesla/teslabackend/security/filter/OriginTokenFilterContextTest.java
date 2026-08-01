package com.tesla.teslabackend.security.filter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprueba que el filtro se puede cablear con el {@code ObjectMapper} que
 * autoconfigura Spring Boot.
 *
 * <p>El resto de tests construyen el filtro a mano, por lo que no cubren la
 * inyeccion de dependencias. Esa laguna dejo pasar a produccion un filtro que
 * pedia el {@code ObjectMapper} de Jackson 2 ({@code com.fasterxml.jackson}),
 * presente en el classpath pero sin bean asociado: Spring Boot 4 autoconfigura
 * el de Jackson 3 ({@code tools.jackson}). La aplicacion compilaba, los tests
 * pasaban y el contenedor moria al arrancar.</p>
 */
class OriginTokenFilterContextTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(OriginTokenFilter.class);

    @Test
    void elFiltroSeCableaConElObjectMapperAutoconfigurado() {
        runner.withPropertyValues("app.security.origin-token=token-de-prueba")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .hasSingleBean(OriginTokenFilter.class));
    }

    @Test
    void elFiltroArrancaTambienSinTokenConfigurado() {
        // Sin la propiedad, el filtro queda desactivado pero el contexto debe levantar.
        runner.run(context -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(OriginTokenFilter.class));
    }
}
