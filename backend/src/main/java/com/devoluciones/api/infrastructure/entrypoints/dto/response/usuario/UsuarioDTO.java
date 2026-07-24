package com.devoluciones.api.infrastructure.entrypoints.dto.response.usuario;

public record UsuarioDTO(
        String username,
        String rol,
        String email) {

}
