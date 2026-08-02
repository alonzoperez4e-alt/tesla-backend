package com.tesla.teslabackend.group.service;

import com.tesla.teslabackend.group.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Borra los mensajes de chat caducados: la conversacion de grupo es efimera y
 * solo conserva la ultima hora.
 *
 * <p><b>Desactivado por defecto.</b> El chat quedo aplazado al retirar Amazon MQ:
 * API Gateway HTTP no puede hacer upgrade de WebSocket, asi que {@code /ws-chat/*}
 * ya no es alcanzable a traves de CloudFront y nadie escribe en la tabla. Con la
 * tarea activa el barrido corria cada minuto y, en el perfil {@code dev}
 * ({@code spring.jpa.show-sql=true}), publicaba el DELETE de Hibernate en
 * CloudWatch: unas 8.600 lineas al dia de puro ruido.</p>
 *
 * <p>No se elimina porque la retencion de una hora es parte del diseño del chat,
 * no un detalle accesorio: si vuelve a habilitarse sin esta limpieza,
 * {@code chat_messages} crece sin limite. Basta con poner
 * {@code app.chat.cleanup.enabled=true} para recuperarla.</p>
 *
 * <p>Sin bloqueo distribuido: ShedLock usaba Redis (ElastiCache) como lock store y
 * se elimino en la optimizacion FinOps. La deduplicacion entre instancias ya no
 * es necesaria porque el servicio ECS corre una unica tarea
 * ({@code desired_count = 1}, sin autoscaling). Si se vuelve a escalar en
 * horizontal, varias tareas ejecutarian esta limpieza a la vez y hara falta
 * reintroducir un lock.</p>
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.chat.cleanup.enabled", havingValue = "true")
public class GroupChatCleanupService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * Con una retencion de una hora, barrer cada minuto no aporta nada: solo
     * adelanta el borrado unos segundos a cambio de 60 veces mas consultas. El
     * intervalo por defecto (15 min) deja los mensajes como mucho 1h15m.
     */
    @Scheduled(fixedRateString = "${app.chat.cleanup.intervalo-ms:900000}")
    @Transactional
    public void cleanOldChatMessages() {

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        chatMessageRepository.deleteOlderThan(oneHourAgo);
    }
}
