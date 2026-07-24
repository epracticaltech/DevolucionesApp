package com.devoluciones.api.infrastructure.entrypoints.dto.response;

import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para listar el historial de eventos de auditoría inmutables de una solicitud.
 */
public record EventoSolicitudResponseDTO(
        Long id,
        Long solicitudId,
        EstadoSolicitud estadoOrigen,
        EstadoSolicitud estadoDestino,
        String usuario,
        LocalDateTime fecha,
        String comentario
) {
}
