package com.devoluciones.api.infrastructure.entrypoints.dto.response;

import java.time.LocalDateTime;

/**
 * Formato de error único exigido por la prueba técnica (Punto 4 de la Parte 1).
 */
public record ErrorResponseDTO(
    LocalDateTime timestamp,
    int status,
    String error,
    String detalle,
    String path
) {}
