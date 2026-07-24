package com.devoluciones.api.infrastructure.entrypoints.dto.response;

import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitudResponseDTO(
    Long id,
    String folio,
    String rutCliente,
    String nombreCliente,
    BigDecimal monto,
    String moneda,
    String bancoDestino,
    String cuentaDestino,
    String referenciaBanco,
    OrigenSolicitud origen,
    EstadoSolicitud estado,
    String motivoRechazo,
    Integer vecesReabierta,
    String creadaPor,
    LocalDateTime fechaCreacion,
    String actualizadaPor,
    LocalDateTime fechaActualizacion
) {}
