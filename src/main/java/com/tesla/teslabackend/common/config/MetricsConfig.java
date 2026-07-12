package com.tesla.teslabackend.common.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.micrometer.core.instrument.config.MeterFilter;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

/**
 * Configuracion de metricas de aplicacion hacia CloudWatch (solo perfil prod).
 *
 * <p>El push de Micrometer no requiere exponer ningun endpoint web: Actuator
 * sigue publicando unicamente {@code health,info}. Spring Boot autoconfigura el
 * {@code CloudWatchMeterRegistry} solo si existe un {@link CloudWatchAsyncClient}
 * en el contexto, por eso se define aqui.</p>
 *
 * <p>La allow-list controla el costo: cada metrica custom de CloudWatch se
 * factura por separado, asi que solo se publican las series relevantes para
 * operar la API (HTTP, JVM, GC, pool de conexiones y CPU).</p>
 */
@Configuration
@Profile("prod")
public class MetricsConfig {

    @Value("${aws.region}")
    private String awsRegion;

    /**
     * Prefijos de metricas que se exportan a CloudWatch. Cualquier metrica cuyo
     * nombre no empiece por uno de estos prefijos se descarta antes de publicar.
     */
    private static final Set<String> ALLOWED_METRIC_PREFIXES = Set.of(
            "http.server.requests",     // latencia/throughput por endpoint y status
            "jvm.memory",               // heap/non-heap usado y comprometido
            "jvm.gc",                    // pausas y ciclos de GC
            "jvm.threads",               // hilos vivos/bloqueados
            "hikaricp.connections",      // pool: activas, idle, pending, timeout
            "process.cpu",               // uso de CPU del proceso
            "system.cpu"                 // uso de CPU del host
    );

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient(DefaultCredentialsProvider credentialsProvider) {
        return CloudWatchAsyncClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /**
     * Deja pasar solo las metricas de {@link #ALLOWED_METRIC_PREFIXES} y descarta
     * el resto del catalogo de Micrometer para acotar el costo en CloudWatch.
     */
    @Bean
    public MeterFilter cloudWatchAllowlistFilter() {
        return MeterFilter.denyUnless(id ->
                ALLOWED_METRIC_PREFIXES.stream().anyMatch(prefix -> id.getName().startsWith(prefix)));
    }
}
