package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.pagination.PaginaResultado;
import com.devoluciones.api.core.domain.models.pagination.SolicitudFiltro;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
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

    @Test
    @DisplayName("Obtener historial de eventos para solicitud existente retorna lista ordenada")
    void obtenerHistorialSolicitudExistenteExito() {
        Long id = 1L;
        Solicitud s = Solicitud.builder().id(id).folio("DEV-2026-000001").build();

        EventoSolicitud evento1 = EventoSolicitud.builder()
                .id(101L)
                .solicitudId(id)
                .estadoOrigen(null)
                .estadoDestino(EstadoSolicitud.BORRADOR)
                .usuario("analista1")
                .fecha(LocalDateTime.now().minusHours(2))
                .comentario("Creación manual")
                .build();

        EventoSolicitud evento2 = EventoSolicitud.builder()
                .id(102L)
                .solicitudId(id)
                .estadoOrigen(EstadoSolicitud.BORRADOR)
                .estadoDestino(EstadoSolicitud.EN_REVISION)
                .usuario("analista1")
                .fecha(LocalDateTime.now().minusHours(1))
                .comentario("Enviada a revisión")
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(s));
        Mockito.when(solicitudRepository.obtenerHistorialPorSolicitudId(id)).thenReturn(List.of(evento1, evento2));

        List<EventoSolicitud> historial = useCase.obtenerHistorial(id);

        assertNotNull(historial);
        assertEquals(2, historial.size());
        assertEquals(EstadoSolicitud.BORRADOR, historial.get(0).getEstadoDestino());
        assertEquals(EstadoSolicitud.EN_REVISION, historial.get(1).getEstadoDestino());
    }

    @Test
    @DisplayName("Obtener historial de solicitud inexistente lanza RecursoNoEncontradoException (HTTP 404)")
    void obtenerHistorialSolicitudInexistenteLanza404() {
        Long id = 999L;
        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtenerHistorial(id));
        Mockito.verify(solicitudRepository, Mockito.never()).obtenerHistorialPorSolicitudId(any());
    }
}
