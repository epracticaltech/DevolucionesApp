package com.devoluciones.api.core.domain.models.cargas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargaMasiva {

    private Long id;
    private String nombreArchivo;
    private int totalFilas;
    private int filasProcesadas;
    private int filasRechazadas;

    @Builder.Default
    private String estado = "PROCESANDO";

    private LocalDateTime fechaCarga;

    @Builder.Default
    private List<DetalleCargaError> errores = new ArrayList<>();
}
