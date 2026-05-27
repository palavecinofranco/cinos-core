package org.cinos.core.auth.controller.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterResponse(Long id, String username, String email, String name, List<String> roles, String accessToken, String refreshToken) {
}
