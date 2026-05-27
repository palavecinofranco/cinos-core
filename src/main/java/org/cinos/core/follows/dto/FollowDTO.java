package org.cinos.core.follows.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FollowDTO(Long id, Long fromUserId, Long toUserId, LocalDateTime createdAt) {
}
