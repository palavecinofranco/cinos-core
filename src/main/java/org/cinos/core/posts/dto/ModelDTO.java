package org.cinos.core.posts.dto;

import lombok.Builder;

@Builder
public record ModelDTO(
        Long id,
        String name) {
}
