package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.ReglaNegocioException;
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

class GestionarTransicionesRevisionUseCaseTest {

    private SolicitudRepositoryPort solicitudRepository;
    private GestionarTransicionesRevisionUseCase useCase;

    private final Usuario supervisor = Usuario.builder()
            .id(1L)
            .username("supervisor1")
            .rol("SUPERVISOR")
            .mail("supervisor1@devoluciones.cl")
            .build();

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        useCase = new GestionarTransicionesRevisionUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Aprobar solicitud en EN_REVISION resulta en estado APROBADA")
    void aprobarExito() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.EN_REVISION)
                .creadaPor("analista1")
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));
        Mockito.when(solicitudRepository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud aprobada = useCase.aprobar(id, supervisor, "Aprobada por revisión completa");

        assertNotNull(aprobada);
        assertEquals(EstadoSolicitud.APROBADA, aprobada.getEstado());
        assertEquals("supervisor1", aprobada.getActualizadaPor());

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("R7: Supervisor que creó la solicitud intenta aprobarla y lanza ReglaNegocioException (HTTP 400)")
    void aprobarMismoUsuarioCreadorLanzaExcepcion400() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.EN_REVISION)
                .creadaPor("supervisor1")
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));

        assertThrows(ReglaNegocioException.class, () -> useCase.aprobar(id, supervisor, "Intentando auto-aprobar"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }

    @Test
    @DisplayName("R1: Aprobar solicitud en estado diferente a EN_REVISION (ej. BORRADOR) lanza TransicionInvalidaException (HTTP 409)")
    void aprobarEstadoInvalidoLanzaExcepcion409() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.BORRADOR)
                .creadaPor("analista1")
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));

        assertThrows(TransicionInvalidaException.class, () -> useCase.aprobar(id, supervisor, "Aprobar desde borrador"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }

    @Test
    @DisplayName("Rechazar solicitud en EN_REVISION asigna estado RECHAZADA y motivo")
    void rechazarExito() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.EN_REVISION)
                .creadaPor("analista1")
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));
        Mockito.when(solicitudRepository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud rechazada = useCase.rechazar(id, supervisor, "Monto inconsistente con liquidación", "Rechazo formal");

        assertNotNull(rechazada);
        assertEquals(EstadoSolicitud.RECHAZADA, rechazada.getEstado());
        assertEquals("Monto inconsistente con liquidación", rechazada.getMotivoRechazo());

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("R3: Rechazar sin motivo (nulo o blanco) lanza ReglaNegocioException (HTTP 400)")
    void rechazarSinMotivoLanzaExcepcion400() {
        Long id = 1L;

        assertThrows(ReglaNegocioException.class, () -> useCase.rechazar(id, supervisor, "   ", "Comentario"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }

    @Test
    @DisplayName("R1: Rechazar solicitud en estado diferente a EN_REVISION lanza TransicionInvalidaException (HTTP 409)")
    void rechazarEstadoInvalidoLanzaExcepcion409() {
        Long id = 1L;
        Solicitud solicitud = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.BORRADOR)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(solicitud));

        assertThrows(TransicionInvalidaException.class, () -> useCase.rechazar(id, supervisor, "Motivo válido", "Comentario"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }
}
