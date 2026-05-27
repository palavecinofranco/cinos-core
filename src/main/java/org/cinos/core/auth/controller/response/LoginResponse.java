package org.cinos.core.auth.controller.response;

import lombok.Builder;

import java.util.List;

@Builder
public record LoginResponse(String username, String email, String name, String lastname, List<String> roles, String accessToken, String refreshToken) {
}
