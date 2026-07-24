package com.devoluciones.api.infrastructure.adapters.postgres.entities;

import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String folio;

    @Column(name = "rut_cliente", nullable = false, length = 12)
    private String rutCliente;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(name = "banco_destino", nullable = false, length = 50)
    private String bancoDestino;

    @Column(name = "cuenta_destino", nullable = false, length = 30)
    private String cuentaDestino;

    @Column(name = "referencia_banco", length = 50)
    private String referenciaBanco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigenSolicitud origen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @Column(name = "veces_reabierta", nullable = false)
    private Integer vecesReabierta;

    @Column(name = "creada_por", nullable = false, length = 50)
    private String creadaPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "actualizada_por", length = 50)
    private String actualizadaPor;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
