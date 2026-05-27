package org.cinos.core.posts.dto;

import lombok.Builder;

@Builder
public record MakeDTO(
        Long id,
        String name
) {
}
