package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.pagination.PaginaResultado;
import com.devoluciones.api.core.domain.models.pagination.SolicitudFiltro;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;

/**
 * Caso de Uso: Consultar la bandeja de solicitudes paginada y filtrada (GET /api/v1/solicitudes).
 */
public class ListarSolicitudesUseCase {

    private final SolicitudRepositoryPort solicitudRepository;

    public ListarSolicitudesUseCase(SolicitudRepositoryPort solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public PaginaResultado<Solicitud> ejecutar(SolicitudFiltro filtro, int pagina, int tamano) {
        int paginaAjustada = Math.max(pagina, 0);
        int tamanoAjustado = (tamano <= 0) ? 10 : Math.min(tamano, 100);

        return solicitudRepository.buscarConFiltrosYPaginacion(filtro, paginaAjustada, tamanoAjustado);
    }
}
