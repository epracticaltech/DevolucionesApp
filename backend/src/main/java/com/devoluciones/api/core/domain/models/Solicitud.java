package com.devoluciones.api.core.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {

    private Long id;
    private String folio;
    private String rutCliente;
    private String nombreCliente;
    private BigDecimal monto;
    private String moneda;
    private String bancoDestino;
    private String cuentaDestino;
    private String referenciaBanco;
    private OrigenSolicitud origen;
    private EstadoSolicitud estado;
    private String motivoRechazo;
    private Integer vecesReabierta;

    // Auditoría
    private String creadaPor;
    private LocalDateTime fechaCreacion;
    private String actualizadaPor;
    private LocalDateTime fechaActualizacion;

}
