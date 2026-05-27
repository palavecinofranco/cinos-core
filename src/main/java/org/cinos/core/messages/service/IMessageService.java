package org.cinos.core.messages.service;

import org.cinos.core.messages.dto.MessageDTO;
import org.cinos.core.messages.entity.MessageEntity;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;

import java.util.List;

public interface IMessageService {
    MessageDTO sendMessage(Long senderId, Long recipientId, String content) throws UserNotFoundException;
    void markAsSeen(Long messageId);

    List<MessageEntity> getMessagesByConversation(Long conversationId);
}
