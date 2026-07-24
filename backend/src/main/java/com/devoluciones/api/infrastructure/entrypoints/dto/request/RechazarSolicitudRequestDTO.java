package com.devoluciones.api.infrastructure.entrypoints.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para la acción de rechazo de solicitudes (POST /api/v1/solicitudes/{id}/rechazar).
 */
public record RechazarSolicitudRequestDTO(

        @NotBlank(message = "El motivo de rechazo es obligatorio")
        String motivoRechazo,

        String comentario
) {
}
