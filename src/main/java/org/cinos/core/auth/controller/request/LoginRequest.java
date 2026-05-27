package org.cinos.core.auth.controller.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El usuario no puede estar vacio")
        String username,
        @NotBlank(message = "La contraseña no puede estar vacia")
        String password) {
}
