package com.devoluciones.api.core.usecase.solicitudes;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ListarSolicitudesUseCaseTest {

    private SolicitudRepositoryPort solicitudRepository;
    private ListarSolicitudesUseCase listarSolicitudesUseCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        listarSolicitudesUseCase = new ListarSolicitudesUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Listar solicitudes aplica correctamente limites de paginacion y consulta puerto")
    void listarSolicitudesPaginacionValida() {
        SolicitudFiltro filtro = SolicitudFiltro.builder()
                .estado(EstadoSolicitud.BORRADOR)
                .build();

        PaginaResultado<Solicitud> paginaMock = new PaginaResultado<>(
                List.of(Solicitud.builder().id(1L).estado(EstadoSolicitud.BORRADOR).build()),
                0,
                10,
                1L,
                1
        );

        Mockito.when(solicitudRepository.buscarConFiltrosYPaginacion(any(SolicitudFiltro.class), eq(0), eq(10)))
                .thenReturn(paginaMock);

        PaginaResultado<Solicitud> resultado = listarSolicitudesUseCase.ejecutar(filtro, 0, 10);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContenido().size());
        assertEquals(1L, resultado.getTotalElementos());

        Mockito.verify(solicitudRepository, Mockito.times(1))
                .buscarConFiltrosYPaginacion(filtro, 0, 10);
    }
}
