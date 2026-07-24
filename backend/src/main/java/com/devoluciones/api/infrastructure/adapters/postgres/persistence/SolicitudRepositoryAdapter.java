package com.devoluciones.api.infrastructure.adapters.postgres.persistence;

import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.pagination.PaginaResultado;
import com.devoluciones.api.core.domain.models.pagination.SolicitudFiltro;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.infrastructure.adapters.postgres.entities.EventoSolicitudEntity;
import com.devoluciones.api.infrastructure.adapters.postgres.entities.SolicitudEntity;
import com.devoluciones.api.infrastructure.adapters.postgres.repositories.EventoSolicitudJpaRepository;
import com.devoluciones.api.infrastructure.adapters.postgres.repositories.SolicitudJpaRepository;
import com.devoluciones.api.infrastructure.adapters.postgres.specifications.SolicitudSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class SolicitudRepositoryAdapter implements SolicitudRepositoryPort {

    private final SolicitudJpaRepository solicitudJpaRepository;
    private final EventoSolicitudJpaRepository eventoSolicitudJpaRepository;

    public SolicitudRepositoryAdapter(
            SolicitudJpaRepository solicitudJpaRepository,
            EventoSolicitudJpaRepository eventoSolicitudJpaRepository) {
        this.solicitudJpaRepository = solicitudJpaRepository;
        this.eventoSolicitudJpaRepository = eventoSolicitudJpaRepository;
    }

    @Override
    public Solicitud guardar(Solicitud solicitud) {
        SolicitudEntity entity = toEntity(solicitud);
        SolicitudEntity guardada = solicitudJpaRepository.save(entity);
        return toDomain(guardada);
    }

    @Override
    public List<Solicitud> guardarTodas(List<Solicitud> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            return List.of();
        }
        List<SolicitudEntity> entities = solicitudes.stream().map(this::toEntity).toList();
        List<SolicitudEntity> guardadas = solicitudJpaRepository.saveAll(entities);
        return guardadas.stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Solicitud> buscarPorId(Long id) {
        return solicitudJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Solicitud> buscarPorFolio(String folio) {
        return solicitudJpaRepository.findByFolio(folio).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorReferenciaBanco(String referenciaBanco) {
        return solicitudJpaRepository.existsByReferenciaBanco(referenciaBanco);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResultado<Solicitud> buscarConFiltros(SolicitudFiltro filtro) {
        return buscarConFiltrosYPaginacion(filtro, filtro.getPagina(), filtro.getTamano());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResultado<Solicitud> buscarConFiltrosYPaginacion(SolicitudFiltro filtro, int pagina, int tamano) {
        Specification<SolicitudEntity> spec = Specification
                .where(SolicitudSpecifications.conEstado(filtro.getEstado()))
                .and(SolicitudSpecifications.conRut(filtro.getRutCliente()))
                .and(SolicitudSpecifications.conOrigen(filtro.getOrigen()))
                .and(SolicitudSpecifications.entreFechas(filtro.getFechaDesde(), filtro.getFechaHasta()));

        PageRequest pageRequest = PageRequest.of(pagina, tamano, Sort.by("fechaCreacion").descending());
        Page<SolicitudEntity> page = solicitudJpaRepository.findAll(spec, pageRequest);

        List<Solicitud> contenidoDominio = page.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new PaginaResultado<>(
                contenidoDominio,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public EventoSolicitud registrarEvento(EventoSolicitud evento) {
        EventoSolicitudEntity entity = toEntity(evento);
        EventoSolicitudEntity guardado = eventoSolicitudJpaRepository.save(entity);
        return toDomain(guardado);
    }

    @Override
    public void registrarEventos(List<EventoSolicitud> eventos) {
        if (eventos == null || eventos.isEmpty()) {
            return;
        }
        List<EventoSolicitudEntity> entities = eventos.stream().map(this::toEntity).toList();
        eventoSolicitudJpaRepository.saveAll(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoSolicitud> obtenerHistorialEventos(Long solicitudId) {
        return obtenerHistorialPorSolicitudId(solicitudId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoSolicitud> obtenerHistorialPorSolicitudId(Long solicitudId) {
        return eventoSolicitudJpaRepository.findBySolicitudIdOrderByFechaAsc(solicitudId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    // --- Mappers ---
    private SolicitudEntity toEntity(Solicitud s) {
        return SolicitudEntity.builder()
                .id(s.getId())
                .folio(s.getFolio())
                .rutCliente(s.getRutCliente())
                .nombreCliente(s.getNombreCliente())
                .monto(s.getMonto())
                .moneda(s.getMoneda())
                .bancoDestino(s.getBancoDestino())
                .cuentaDestino(s.getCuentaDestino())
                .referenciaBanco(s.getReferenciaBanco())
                .origen(s.getOrigen())
                .estado(s.getEstado())
                .motivoRechazo(s.getMotivoRechazo())
                .vecesReabierta(s.getVecesReabierta() != null ? s.getVecesReabierta() : 0)
                .creadaPor(s.getCreadaPor())
                .fechaCreacion(s.getFechaCreacion())
                .actualizadaPor(s.getActualizadaPor())
                .fechaActualizacion(s.getFechaActualizacion())
                .build();
    }

    private Solicitud toDomain(SolicitudEntity e) {
        return Solicitud.builder()
                .id(e.getId())
                .folio(e.getFolio())
                .rutCliente(e.getRutCliente())
                .nombreCliente(e.getNombreCliente())
                .monto(e.getMonto())
                .moneda(e.getMoneda())
                .bancoDestino(e.getBancoDestino())
                .cuentaDestino(e.getCuentaDestino())
                .referenciaBanco(e.getReferenciaBanco())
                .origen(e.getOrigen())
                .estado(e.getEstado())
                .motivoRechazo(e.getMotivoRechazo())
                .vecesReabierta(e.getVecesReabierta())
                .creadaPor(e.getCreadaPor())
                .fechaCreacion(e.getFechaCreacion())
                .actualizadaPor(e.getActualizadaPor())
                .fechaActualizacion(e.getFechaActualizacion())
                .build();
    }

    private EventoSolicitudEntity toEntity(EventoSolicitud e) {
        return EventoSolicitudEntity.builder()
                .id(e.getId())
                .solicitudId(e.getSolicitudId())
                .estadoOrigen(e.getEstadoOrigen())
                .estadoDestino(e.getEstadoDestino())
                .usuario(e.getUsuario())
                .fecha(e.getFecha())
                .comentario(e.getComentario())
                .build();
    }

    private EventoSolicitud toDomain(EventoSolicitudEntity e) {
        return EventoSolicitud.builder()
                .id(e.getId())
                .solicitudId(e.getSolicitudId())
                .estadoOrigen(e.getEstadoOrigen())
                .estadoDestino(e.getEstadoDestino())
                .usuario(e.getUsuario())
                .fecha(e.getFecha())
                .comentario(e.getComentario())
                .build();
    }
}
