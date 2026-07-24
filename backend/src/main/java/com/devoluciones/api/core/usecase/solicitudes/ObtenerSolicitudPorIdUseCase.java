package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;

/**
 * Caso de Uso: Obtener una Solicitud por su ID (GET /api/v1/solicitudes/{id}).
 */
public class ObtenerSolicitudPorIdUseCase {

    private final SolicitudRepositoryPort solicitudRepository;

    public ObtenerSolicitudPorIdUseCase(SolicitudRepositoryPort solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public Solicitud ejecutar(Long id) {
        return solicitudRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la solicitud con el ID: " + id));
    }
}
