package com.devoluciones.api.infrastructure.entrypoints.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para la acción de rechazo de solicitudes (POST /api/v1/solicitudes/{id}/rechazar).
 */
public record RechazarSolicitudRequestDTO(

        @NotNull(message = "El usuario que realiza la operación es obligatorio")
        @Valid
        UsuarioAutenticadoDto usuario,

        @NotBlank(message = "El motivo de rechazo es obligatorio")
        String motivoRechazo,

        String comentario
) {
}
