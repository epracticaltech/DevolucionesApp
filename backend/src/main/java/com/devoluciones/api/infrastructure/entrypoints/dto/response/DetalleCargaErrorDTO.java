package com.devoluciones.api.infrastructure.entrypoints.dto.response;

public record DetalleCargaErrorDTO(
        int fila,
        String campo,
        String motivo
) {
}
