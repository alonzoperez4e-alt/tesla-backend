package com.tesla.teslabackend.group.service;

import com.tesla.teslabackend.group.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupChatCleanupService {

    private final ChatMessageRepository chatMessageRepository;

    // Sin bloqueo distribuido: ShedLock usaba Redis (ElastiCache) como lock store y
    // se elimino en la optimizacion FinOps. La deduplicacion entre instancias ya no
    // es necesaria porque el servicio ECS corre una unica tarea (desired_count = 1,
    // sin autoscaling). Si se vuelve a escalar en horizontal, varias tareas
    // ejecutarian esta limpieza a la vez y hara falta reintroducir un lock.
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanOldChatMessages() {

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        chatMessageRepository.deleteOlderThan(oneHourAgo);

        // (Opcional) Puedes comentar el System.out para que no te llene la consola de texto cada minuto
        // System.out.println("🧹 Limpieza Efímera: Mensajes con más de 1 hora de antigüedad eliminados.");
    }
}