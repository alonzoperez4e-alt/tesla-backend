package com.tesla.teslabackend.common.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SchedulerLockConfigTest {

    @Test
    void lockProvider_construyeUnRedisLockProvider() {
        // Arrange
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        SchedulerLockConfig config = new SchedulerLockConfig();

        // Act
        LockProvider lockProvider = config.lockProvider(connectionFactory);

        // Assert: se cablea el provider de Redis sin abrir conexion (es perezoso).
        assertThat(lockProvider).isInstanceOf(RedisLockProvider.class);
    }
}
