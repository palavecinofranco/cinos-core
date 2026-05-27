package org.cinos.core.messages.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotNull(message = "El ID del destinatario es obligatorio")
        Long recipientId,

        @NotBlank(message = "El contenido del mensaje no puede estar vacío")
        @Size(max = 2000, message = "El mensaje no puede exceder los 2000 caracteres")
        String content,

        // Opcional: para mensajes que responden a otros mensajes
        Long replyToMessageId
) {
}
