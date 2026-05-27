package org.cinos.core.users.controller.request;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

@Builder
public record UserCreateRequest(
        @NotBlank(message = "El usuario no puede estar vacio")
        @Pattern(regexp = "^(?=(?:[^A-Za-z]*[A-Za-z]){3,})[A-Za-z0-9]{4,}$", message = "El usuario debe tener al menos 4 caracteres y al menos 3 letras")
        String username,
        @NotBlank(message = "El nombre no puede estar vacio")
        String name,
        @NotBlank(message = "El apellido no puede estar vacio")
        String lastname,
        @NotBlank(message = "El email no puede estar vacio")
        String email,
        @NotBlank(message = "La contraseña no puede estar vacia")
        @Pattern(regexp = ".{6,}", message = "La contraseña debe tener al menos 6 caracteres")
        @Pattern(regexp = ".*[0-9].*", message = "La contraseña debe contener al menos un número")
        String password,
        @NotBlank(message = "La contraseña no puede estar vacia")
        String repeatPassword) implements Serializable {
}
