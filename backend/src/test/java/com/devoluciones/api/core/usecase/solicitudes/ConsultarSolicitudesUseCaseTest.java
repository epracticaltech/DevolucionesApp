package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.pagination.PaginaResultado;
import com.devoluciones.api.core.domain.models.pagination.SolicitudFiltro;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ConsultarSolicitudesUseCaseTest {

    private SolicitudRepositoryPort solicitudRepository;
    private ConsultarSolicitudesUseCase useCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        useCase = new ConsultarSolicitudesUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Obtener por ID existente retorna la solicitud")
    void obtenerPorIdExistenteExito() {
        Long id = 1L;
        Solicitud s = Solicitud.builder().id(id).folio("DEV-2026-000001").build();
        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(s));

        Solicitud resultado = useCase.obtenerPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    @DisplayName("Obtener por ID inexistente lanza RecursoNoEncontradoException")
    void obtenerPorIdInexistenteLanzaExcepcion() {
        Long id = 999L;
        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtenerPorId(id));
    }

    @Test
    @DisplayName("Listar con filtros aplica paginacion valida")
    void listarConFiltrosExito() {
        SolicitudFiltro filtro = SolicitudFiltro.builder().estado(EstadoSolicitud.BORRADOR).build();
        PaginaResultado<Solicitud> mockPagina = new PaginaResultado<>(
                List.of(Solicitud.builder().id(1L).build()), 0, 10, 1L, 1
        );

        Mockito.when(solicitudRepository.buscarConFiltrosYPaginacion(any(), eq(0), eq(10)))
                .thenReturn(mockPagina);

        PaginaResultado<Solicitud> resultado = useCase.listarConFiltros(filtro, 0, 10);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContenido().size());
    }
}
