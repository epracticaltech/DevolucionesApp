package com.devoluciones.api.core.domain.models;

import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /**
     * Transiciona el estado de la solicitud apoyándose de forma cohesiva en la Máquina de Estados del Enum.
     * Lanza TransicionInvalidaException (HTTP 409) si la transición no es permitida según la Regla R1.
     */
    public void cambiarEstado(EstadoSolicitud nuevoEstado) {
        if (this.estado != null) {
            this.estado.validarTransicionHacia(nuevoEstado);
        }
        this.estado = nuevoEstado;
    }
}
