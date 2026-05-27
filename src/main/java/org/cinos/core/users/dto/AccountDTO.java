package org.cinos.core.users.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AccountDTO(
        Long id,
        String name,
        String username,
        String lastname,
        String email,
        Integer points,
        Long followers,
        Long followings,
        String avatarImg,
        List<String> roles,
        Boolean hasSeenRecommendationsModal,
        String phone,
        String attentionHours
        ) {
}
