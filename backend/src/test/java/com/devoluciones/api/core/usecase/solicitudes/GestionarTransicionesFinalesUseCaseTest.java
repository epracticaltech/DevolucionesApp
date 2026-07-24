package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.AccesoDenegadoException;
import com.devoluciones.api.core.domain.exceptions.TransicionInvalidaException;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.UsuarioAutenticadoDto;
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

    private final UsuarioAutenticadoDto supervisor = new UsuarioAutenticadoDto("supervisor1", "SUPERVISOR");
    private final UsuarioAutenticadoDto analista = new UsuarioAutenticadoDto("analista1", "ANALISTA");

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        useCase = new GestionarTransicionesFinalesUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Pagar solicitud en estado APROBADA con rol SUPERVISOR transiciona a PAGADA y guarda evento")
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
    @DisplayName("R2: Pagar solicitud con rol ANALISTA lanza AccesoDenegadoException (HTTP 403)")
    void pagarRolAnalistaLanzaExcepcion403() {
        Long id = 1L;

        assertThrows(AccesoDenegadoException.class, () -> useCase.pagar(id, analista, "Intentando pagar como analista"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
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
                .vecesReabierta(1) // Ya fue reabierta 1 vez
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
