package org.cinos.core.messages.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cinos.core.messages.dto.ConversationDTO;
import org.cinos.core.messages.dto.MessageDTO;
import org.cinos.core.messages.dto.SendMessageRequest;
import org.cinos.core.messages.entity.MessageEntity;
import org.cinos.core.messages.service.IConversationService;
import org.cinos.core.messages.service.IMessageService;
import org.cinos.core.users.service.IUserService;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final IMessageService messageService;
    private final IUserService userService;
    private final IConversationService conversationService;

    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestBody @Valid SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) throws UserNotFoundException {

        Long senderId = userService.getByUsername(userDetails.getUsername()).id();
        MessageDTO message = messageService.sendMessage(
                senderId,
                request.recipientId(),
                request.content()
        );

        return ResponseEntity.ok(message);
    }

    @GetMapping("/conversation/messages/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByConversation(
            @PathVariable Long conversationId) {

        List<MessageEntity> messages = messageService.getMessagesByConversation(conversationId);

        List<MessageDTO> dtos = messages.stream()
                .map(message -> new MessageDTO(
                        message.getId(),
                        message.getSender().getId(),
                        message.getRecipient().getId(),
                        message.getContent(),
                        message.getTimestamp().atZone(ZoneId.systemDefault())
                ))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationDTO>> getConversations(@PathVariable final Long userId) {
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }

    @GetMapping("/conversation/{id}")
    public ResponseEntity<ConversationDTO> getConversation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(conversationService.getById(id, userDetails.getUsername()));
    }

    @GetMapping("/conversation/between")
    public ResponseEntity<ConversationDTO> getConversationBetween(
            @RequestParam Long userSendId,
            @RequestParam Long userReceivedId) throws UserNotFoundException {
        ConversationDTO conversation = conversationService.getOrCreateConversation(userSendId, userReceivedId);
        return ResponseEntity.ok(conversation);
    }
}
