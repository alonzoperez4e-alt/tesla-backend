package com.tesla.teslabackend.group.service;

import com.tesla.teslabackend.group.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupChatCleanupService {

    private final ChatMessageRepository chatMessageRepository;

    // ShedLock: solo una tarea ECS ejecuta la limpieza por ventana. El lock se
    // sostiene ~50-55s (casi toda la ventana de 60s) para deduplicar los
    // disparos casi simultaneos de las varias instancias sin bloquear la
    // siguiente ejecucion; si la instancia muere, el lock expira a los 55s.
    @Scheduled(fixedRate = 60000)
    @SchedulerLock(name = "cleanOldChatMessages", lockAtLeastFor = "PT50S", lockAtMostFor = "PT55S")
    @Transactional
    public void cleanOldChatMessages() {

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        chatMessageRepository.deleteOlderThan(oneHourAgo);

        // (Opcional) Puedes comentar el System.out para que no te llene la consola de texto cada minuto
        // System.out.println("🧹 Limpieza Efímera: Mensajes con más de 1 hora de antigüedad eliminados.");
    }
}