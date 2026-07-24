package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.pagination.PaginaResultado;
import com.devoluciones.api.core.domain.models.pagination.SolicitudFiltro;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;

/**
 * Caso de Uso Consolidado: Consultar solicitudes por ID y listar solicitudes paginadas con filtros.
 */
public class ConsultarSolicitudesUseCase {

    private final SolicitudRepositoryPort solicitudRepository;

    public ConsultarSolicitudesUseCase(SolicitudRepositoryPort solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public Solicitud obtenerPorId(Long id) {
        return solicitudRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la solicitud con el ID: " + id));
    }

    public PaginaResultado<Solicitud> listarConFiltros(SolicitudFiltro filtro, int pagina, int tamano) {
        int paginaAjustada = Math.max(pagina, 0);
        int tamanoAjustado = (tamano <= 0) ? 10 : Math.min(tamano, 100);

        return solicitudRepository.buscarConFiltrosYPaginacion(filtro, paginaAjustada, tamanoAjustado);
    }
}
