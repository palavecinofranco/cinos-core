package org.cinos.core.posts.dto;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record CommentDTO(Long id, Long postId, Long userId, String content, ZonedDateTime commentDate, String accountAvatar, String userName) { }
