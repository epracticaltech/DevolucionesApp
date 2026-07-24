package com.devoluciones.api.core.domain.port;

import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.pagination.PaginaResultado;
import com.devoluciones.api.core.domain.models.pagination.SolicitudFiltro;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepositoryPort {

    Solicitud guardar(Solicitud solicitud);

    List<Solicitud> guardarTodas(List<Solicitud> solicitudes);

    Optional<Solicitud> buscarPorId(Long id);

    Optional<Solicitud> buscarPorFolio(String folio);

    boolean existePorReferenciaBanco(String referenciaBanco);

    PaginaResultado<Solicitud> buscarConFiltrosYPaginacion(SolicitudFiltro filtro, int pagina, int tamano);

    EventoSolicitud registrarEvento(EventoSolicitud evento);

    void registrarEventos(List<EventoSolicitud> eventos);

    List<EventoSolicitud> obtenerHistorialPorSolicitudId(Long solicitudId);
}
