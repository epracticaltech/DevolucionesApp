package com.devoluciones.api.infrastructure.entrypoints.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UsuarioAutenticadoDto(
        @NotBlank(message = "El nombre del usuario es obligatorio")
        String username,

        String rol
) {
}
