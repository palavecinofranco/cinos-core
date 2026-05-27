package org.cinos.core.messages.dto;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record ConversationDTO(
        Long id,
        ZonedDateTime lastUpdated,
        String lastMessage,
        Long receiverId,
        String receiverName,
        String receiverAvatar) {}
