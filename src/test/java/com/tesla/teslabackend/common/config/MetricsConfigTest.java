package com.tesla.teslabackend.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsConfigTest {

    private static final MeterFilter FILTER = new MetricsConfig().cloudWatchAllowlistFilter();

    private static MeterFilterReply replyFor(String meterName) {
        Meter.Id id = new Meter.Id(meterName, Tags.empty(), null, null, Meter.Type.OTHER);
        return FILTER.accept(id);
    }

    @Test
    void allowlistFilter_dejaPasarMetricasPermitidas() {
        // Arrange
        String[] permitidas = {
                "http.server.requests",
                "jvm.memory.used",
                "jvm.gc.pause",
                "hikaricp.connections.pending",
                "process.cpu.usage",
                "system.cpu.usage"
        };

        // Act & Assert
        for (String metrica : permitidas) {
            assertThat(replyFor(metrica))
                    .as("la metrica %s deberia pasar el filtro", metrica)
                    .isNotEqualTo(MeterFilterReply.DENY);
        }
    }

    @Test
    void allowlistFilter_descartaMetricasFueraDeLaAllowlist() {
        // Arrange
        String[] descartadas = {
                "logback.events",
                "tomcat.sessions.active.current",
                "spring.data.repository.invocations"
        };

        // Act & Assert
        for (String metrica : descartadas) {
            assertThat(replyFor(metrica))
                    .as("la metrica %s deberia ser descartada", metrica)
                    .isEqualTo(MeterFilterReply.DENY);
        }
    }

    @Test
    void cloudWatchAsyncClient_seConstruyeConLaRegionConfigurada() {
        // Arrange
        MetricsConfig config = new MetricsConfig();
        ReflectionTestUtils.setField(config, "awsRegion", "us-east-1");

        // Act
        try (CloudWatchAsyncClient client = config.cloudWatchAsyncClient(DefaultCredentialsProvider.create())) {
            // Assert: el bean se construye sin resolver credenciales/red ("monitoring"
            // es el nombre interno del servicio de la API de CloudWatch).
            assertThat(client).isNotNull();
            assertThat(client.serviceName()).isEqualTo("monitoring");
        }
    }
}
