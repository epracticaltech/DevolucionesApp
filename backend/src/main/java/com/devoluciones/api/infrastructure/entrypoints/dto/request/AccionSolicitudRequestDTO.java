package com.devoluciones.api.infrastructure.entrypoints.dto.request;

/**
 * DTO de entrada para endpoints de acciones sobre solicitudes (enviar, anular, aprobar, pagar, reabrir).
 */
public record AccionSolicitudRequestDTO(
        String comentario
) {
}
