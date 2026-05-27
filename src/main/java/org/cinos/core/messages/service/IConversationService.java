package org.cinos.core.messages.service;

import org.cinos.core.messages.dto.ConversationDTO;
import org.cinos.core.messages.entity.ConversationEntity;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;

import java.util.List;

public interface IConversationService {
    ConversationDTO getOrCreateConversation(Long user1Id, Long user2Id) throws UserNotFoundException;
    List<ConversationDTO> getUserConversations(Long userId);
    ConversationDTO getById(Long id, String username);
    void save(ConversationEntity conversationEntity);
    ConversationEntity getEntityById(Long id);
}
