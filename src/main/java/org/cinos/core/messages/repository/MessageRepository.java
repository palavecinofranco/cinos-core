package org.cinos.core.messages.repository;

import org.cinos.core.messages.entity.MessageEntity;
import org.cinos.core.users.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findByConversationIdOrderByTimestampAsc(Long conversationId);
    Long countByRecipientAndSeen(AccountEntity recipient, boolean seen);
}
