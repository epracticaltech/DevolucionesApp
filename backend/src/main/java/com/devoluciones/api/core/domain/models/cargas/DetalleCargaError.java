package com.devoluciones.api.core.domain.models.cargas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCargaError {

    private Long id;
    private Long cargaId;
    private int numeroFila;
    private String campo;
    private String motivo;
}
