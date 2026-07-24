package com.devoluciones.api.infrastructure.entrypoints.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para endpoints de acciones sobre solicitudes (enviar, anular, aprobar, rechazar, pagar, reabrir).
 */
public record AccionSolicitudRequestDTO(

        @NotNull(message = "El usuario que realiza la operación es obligatorio")
        @Valid
        UsuarioAutenticadoDto usuario,

        String comentario
) {
}
