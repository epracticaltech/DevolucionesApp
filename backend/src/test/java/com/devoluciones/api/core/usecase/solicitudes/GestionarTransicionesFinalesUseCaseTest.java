package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.TransicionInvalidaException;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.Usuario;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class GestionarTransicionesFinalesUseCaseTest {

    private SolicitudRepositoryPort solicitudRepository;
    private GestionarTransicionesFinalesUseCase useCase;

    private final Usuario supervisor = Usuario.builder()
            .id(1L)
            .username("supervisor1")
            .rol("SUPERVISOR")
            .mail("supervisor1@devoluciones.cl")
            .build();

    private final Usuario analista = Usuario.builder()
            .id(2L)
            .username("analista1")
            .rol("ANALISTA")
            .mail("analista1@devoluciones.cl")
            .build();

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        useCase = new GestionarTransicionesFinalesUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Pagar solicitud en estado APROBADA transiciona a PAGADA y guarda evento")
    void pagarExito() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.APROBADA)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));
        Mockito.when(solicitudRepository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud pagada = useCase.pagar(id, supervisor, "Pago emitido por tesorería");

        assertNotNull(pagada);
        assertEquals(EstadoSolicitud.PAGADA, pagada.getEstado());
        assertEquals("supervisor1", pagada.getActualizadaPor());

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("R1: Pagar solicitud en estado distinto a APROBADA lanza TransicionInvalidaException (HTTP 409)")
    void pagarEstadoInvalidoLanzaExcepcion409() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.EN_REVISION)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));

        assertThrows(TransicionInvalidaException.class, () -> useCase.pagar(id, supervisor, "Pagar desde revisión"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }

    @Test
    @DisplayName("Reabrir solicitud en estado RECHAZADA (vecesReabierta = 0) transiciona a BORRADOR y suma contador")
    void reabrirExito() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.RECHAZADA)
                .vecesReabierta(0)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));
        Mockito.when(solicitudRepository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud reabierta = useCase.reabrir(id, analista, "Reabriendo para corregir antecedente");

        assertNotNull(reabierta);
        assertEquals(EstadoSolicitud.BORRADOR, reabierta.getEstado());
        assertEquals(1, reabierta.getVecesReabierta());
        assertEquals("analista1", reabierta.getActualizadaPor());

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("R4: Reabrir solicitud que ya fue reabierta 1 vez lanza TransicionInvalidaException (HTTP 409)")
    void reabrirLimiteExcedidoLanzaExcepcion409() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.RECHAZADA)
                .vecesReabierta(1)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));

        assertThrows(TransicionInvalidaException.class, () -> useCase.reabrir(id, analista, "Segunda reapertura"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }

    @Test
    @DisplayName("R1: Reabrir solicitud en estado diferente a RECHAZADA (ej. APROBADA) lanza TransicionInvalidaException (HTTP 409)")
    void reabrirEstadoInvalidoLanzaExcepcion409() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.APROBADA)
                .vecesReabierta(0)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));

        assertThrows(TransicionInvalidaException.class, () -> useCase.reabrir(id, analista, "Reabrir desde aprobada"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }
}
