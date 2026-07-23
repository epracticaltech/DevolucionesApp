package com.devoluciones.api.core.domain.port;

import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepositoryPort {

    Solicitud guardar(Solicitud solicitud);

    Optional<Solicitud> buscarPorId(Long id);

    Optional<Solicitud> buscarPorFolio(String folio);

    boolean existePorReferenciaBanco(String referenciaBanco);

    EventoSolicitud registrarEvento(EventoSolicitud evento);

    List<EventoSolicitud> obtenerHistorialPorSolicitudId(Long solicitudId);

}
