package org.cinos.core.posts.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PostLocationDTO(
        String address,
        BigDecimal lat,
        BigDecimal lng
) {
}
