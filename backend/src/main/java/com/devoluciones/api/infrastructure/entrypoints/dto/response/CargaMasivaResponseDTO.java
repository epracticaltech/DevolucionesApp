package com.devoluciones.api.infrastructure.entrypoints.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resumen de Carga Masiva exigido por la especificación (GET /api/v1/cargas/{id}).
 */
public record CargaMasivaResponseDTO(
        Long id,
        String nombreArchivo,
        int totalFilas,
        int filasProcesadas,
        int filasRechazadas,
        String estado,
        LocalDateTime fechaCarga,
        List<DetalleCargaErrorDTO> errores
) {
}
