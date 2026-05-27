package org.cinos.core.technical_verification.dto;

public record OrderVerificationRequest(
        Long postId,
        String userPhone
) {
}
