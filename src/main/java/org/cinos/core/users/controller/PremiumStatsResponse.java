package org.cinos.core.users.controller;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PremiumStatsResponse(
    Integer verificationsRemaining,
    Integer verificationReportsRemaining,
    LocalDateTime nextResetDate
) {} 