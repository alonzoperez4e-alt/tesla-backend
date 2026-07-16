package com.tesla.teslabackend.common.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Bloqueo distribuido para tareas @Scheduled (ShedLock) usando Redis (ElastiCache)
 * como almacen de locks.
 *
 * <p>Motivo: con autoscaling el servicio ECS corre varias tareas en paralelo y,
 * sin bloqueo, cada tarea ejecutaria las tareas programadas (p. ej. la limpieza
 * de chat) a la vez. ShedLock asegura que solo una instancia las ejecute por
 * ventana. Se usa el provider de Redis porque ya existe la conexion (rankings) y
 * evita crear una tabla de locks (prod corre con ddl-auto=validate).
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class SchedulerLockConfig {

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory);
    }
}
