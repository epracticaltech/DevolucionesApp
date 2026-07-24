package com.devoluciones.api.infrastructure.entrypoints.dto.response.usuario;

public record AuthResponseDTO(
        String access_token,
        long expires_in,
        String rol,
        String username,
        String email
) {
}
