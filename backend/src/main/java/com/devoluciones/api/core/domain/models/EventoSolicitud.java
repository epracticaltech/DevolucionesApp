package com.devoluciones.api.core.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoSolicitud {

    private Long id;
    private Long solicitudId;
    private EstadoSolicitud estadoOrigen;
    private EstadoSolicitud estadoDestino;
    private String usuario;
    private LocalDateTime fecha;
    private String comentario;

}