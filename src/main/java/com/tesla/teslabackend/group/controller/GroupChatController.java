package com.tesla.teslabackend.group.controller;

import com.tesla.teslabackend.group.entity.ChatMessage;
import com.tesla.teslabackend.group.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GroupChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/{groupId}/chat/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(chatMessageRepository.findByGroupIdOrderByTimestampAsc(groupId));
    }

    @MessageMapping("/chat/{groupId}/sendMessage")
    public void sendMessage(@DestinationVariable Long groupId, @Payload ChatMessage chatMessage) {
        chatMessage.setGroupId(groupId);
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        messagingTemplate.convertAndSend("/topic/group/" + groupId, savedMessage);
    }
}