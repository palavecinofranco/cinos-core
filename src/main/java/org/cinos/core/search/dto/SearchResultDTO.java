package org.cinos.core.search.dto;

public record SearchResultDTO(
        Long id,
        String title,
        String imageUrl,
        String type
) {
}
