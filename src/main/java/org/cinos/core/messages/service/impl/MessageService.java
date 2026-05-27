package org.cinos.core.messages.service.impl;

import lombok.RequiredArgsConstructor;
import org.cinos.core.messages.dto.ConversationDTO;
import org.cinos.core.messages.dto.MessageDTO;
import org.cinos.core.messages.entity.ConversationEntity;
import org.cinos.core.messages.entity.MessageEntity;
import org.cinos.core.messages.model.MessageStatus;
import org.cinos.core.messages.repository.MessageRepository;
import org.cinos.core.messages.service.IConversationService;
import org.cinos.core.messages.service.IMessageService;
import org.cinos.core.users.service.IAccountService;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    private final IConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final IAccountService accountService;

    @Transactional
    public MessageDTO sendMessage(Long senderId, Long recipientId, String content) throws UserNotFoundException {
        ConversationDTO conversation = conversationService.getOrCreateConversation(senderId, recipientId);
        ConversationEntity conversationEntity = conversationService.getEntityById(conversation.id());

        MessageEntity message = MessageEntity.builder()
                .conversation(conversationEntity)
                .sender(accountService.getAccountEntityById(senderId))
                .recipient(accountService.getAccountEntityById(recipientId))
                .content(content)
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();

        MessageEntity savedMessage = messageRepository.save(message);

        MessageDTO messageDTO = new MessageDTO(
                savedMessage.getId(),
                savedMessage.getSender().getId(),
                savedMessage.getRecipient().getId(),
                savedMessage.getContent(),
                savedMessage.getTimestamp().atZone(ZoneId.systemDefault())
        );
        // Actualizar WebSocket
        messagingTemplate.convertAndSendToUser(
                accountService.getAccountEntityById(recipientId).getUser().getUsername(),
                "/queue/messages",
                messageDTO
        );

        conversationEntity.setLastUpdated(LocalDateTime.now());
        conversationService.save(conversationEntity);

        return messageDTO;
    }

    @Transactional
    public void markAsSeen(Long messageId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setSeen(true);
        message.setStatus(MessageStatus.SEEN);
        messageRepository.save(message);
    }

    @Override
    public List<MessageEntity> getMessagesByConversation(Long conversationId) {
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }
}
