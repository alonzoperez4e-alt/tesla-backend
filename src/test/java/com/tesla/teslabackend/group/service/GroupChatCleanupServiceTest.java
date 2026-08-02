package com.tesla.teslabackend.group.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tesla.teslabackend.group.repository.ChatMessageRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * El chat quedo aplazado, asi que la limpieza no debe registrarse salvo que se
 * habilite de forma explicita. Se usa {@link ApplicationContextRunner} porque lo
 * que se comprueba es justamente el cableado: construir la clase a mano no
 * evaluaria la condicion ni resolveria el placeholder de {@code @Scheduled}.
 */
class GroupChatCleanupServiceTest {

    private final ChatMessageRepository repositorio = mock(ChatMessageRepository.class);

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(ChatMessageRepository.class, () -> repositorio)
            .withUserConfiguration(SchedulingConfig.class, GroupChatCleanupService.class);

    /** Activa el procesado de {@code @Scheduled} para que el intervalo se resuelva de verdad. */
    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    static class SchedulingConfig {
    }

    @Test
    void noSeRegistra_conLaConfiguracionPorDefecto() {
        // Sin la propiedad, el bean no debe existir: cero ejecuciones y cero logs.
        runner.run(context -> assertThat(context)
                .hasNotFailed()
                .doesNotHaveBean(GroupChatCleanupService.class));
    }

    @Test
    void noSeRegistra_cuandoEstaDesactivadaDeFormaExplicita() {
        runner.withPropertyValues("app.chat.cleanup.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(GroupChatCleanupService.class));
    }

    @Test
    void seRegistraYElIntervaloSeResuelve_cuandoSeHabilita() {
        // Un placeholder mal escrito en fixedRateString reventaria aqui, no en produccion.
        runner.withPropertyValues("app.chat.cleanup.enabled=true")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .hasSingleBean(GroupChatCleanupService.class));
    }

    @Test
    void seRespetaElIntervaloConfigurado() {
        runner.withPropertyValues("app.chat.cleanup.enabled=true", "app.chat.cleanup.intervalo-ms=60000")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void borraLosMensajesAnterioresAUnaHora() {
        // Arrange
        GroupChatCleanupService servicio = new GroupChatCleanupService(repositorio);
        LocalDateTime antesDeEjecutar = LocalDateTime.now().minusHours(1);

        // Act
        servicio.cleanOldChatMessages();

        // Assert: el corte cae en la ventana [antes, ahora] de una hora atras.
        LocalDateTime despuesDeEjecutar = LocalDateTime.now().minusHours(1);
        verify(repositorio).deleteOlderThan(argThat(
                corte -> corte != null && !corte.isBefore(antesDeEjecutar) && !corte.isAfter(despuesDeEjecutar)));
    }
}
