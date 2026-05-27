package org.cinos.core.users.dto;

import lombok.Builder;

@Builder
public record VerifyCodeResponse(
        Boolean isValid,
        String message

) {
}
