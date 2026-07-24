package com.devoluciones.api.core.domain.models.pagination;

import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Value Object de Dominio que agrupa los criterios opcionales de búsqueda.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudFiltro {

    private EstadoSolicitud estado;
    private String rutCliente;
    private OrigenSolicitud origen;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
}
