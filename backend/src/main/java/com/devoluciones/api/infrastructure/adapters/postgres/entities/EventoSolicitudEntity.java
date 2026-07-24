package com.devoluciones.api.infrastructure.adapters.postgres.entities;

import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_solicitud")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoSolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_origen", nullable = false, length = 20)
    private EstadoSolicitud estadoOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_destino", nullable = false, length = 20)
    private EstadoSolicitud estadoDestino;

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 255)
    private String comentario;
}
