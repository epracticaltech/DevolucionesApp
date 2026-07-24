package com.devoluciones.api.infrastructure.entrypoints.dto.response.solicitudes;

import com.devoluciones.api.infrastructure.entrypoints.dto.response.usuario.UsuarioDTO;

public record SolicitudBaseDto(
    SolicitudResponseDTO solicitud,
    UsuarioDTO usuario ) {
    
}
