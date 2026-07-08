package com.tesla.teslabackend.group.controller;

import com.tesla.teslabackend.group.dto.SendChatMessageRequestDTO;
import com.tesla.teslabackend.group.entity.ChatMessage;
import com.tesla.teslabackend.group.repository.ChatMessageRepository;
import com.tesla.teslabackend.user.component.IdentityExtractor;
import com.tesla.teslabackend.user.entity.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class GroupChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final IdentityExtractor identityExtractor;

    @GetMapping("/{groupId}/chat/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(chatMessageRepository.findByGroupIdOrderByTimestampAsc(groupId));
    }

    // El studentId/studentName se resiguen siempre desde el JWT (igual que GroupController),
    // nunca del payload del cliente: el front llegó a enviar el "sub" de Cognito (UUID) como
    // studentId, lo que rompía la deserializacion a Long antes de que este metodo se ejecutara.
    @MessageMapping("/chat/{groupId}/sendMessage")
    public void sendMessage(@DestinationVariable Long groupId, @Payload SendChatMessageRequestDTO request, Principal principal) {
        try {
            Jwt jwt = ((JwtAuthenticationToken) principal).getToken();
            Usuario usuario = identityExtractor.getUsuario(jwt);

            ChatMessage chatMessage = ChatMessage.builder()
                    .groupId(groupId)
                    .studentId(Long.valueOf(usuario.getIdUsuario()))
                    .studentName((usuario.getNombre() + " " + usuario.getApellido()).trim())
                    .content(request.getContent())
                    .build();

            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            messagingTemplate.convertAndSend("/topic/group/" + groupId, savedMessage);
        } catch (Exception ex) {
            log.error("No se pudo procesar el mensaje de chat del grupo {}: {}", groupId, ex.getMessage(), ex);
            if (principal != null) {
                messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors",
                        Map.of("groupId", groupId, "error", "No se pudo enviar el mensaje. Inténtalo de nuevo."));
            }
        }
    }
}