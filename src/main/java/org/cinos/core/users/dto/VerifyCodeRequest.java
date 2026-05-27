package org.cinos.core.users.dto;

public record VerifyCodeRequest(
        String code,
        String email
) {
}
