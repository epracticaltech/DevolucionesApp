package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.exceptions.TransicionInvalidaException;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class GestionarTransicionesBorradorUseCaseTest {

    private SolicitudRepositoryPort solicitudRepository;
    private GestionarTransicionesBorradorUseCase useCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        useCase = new GestionarTransicionesBorradorUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Enviar a revision desde BORRADOR transiciona a EN_REVISION y registra evento auditoria")
    void enviarARevisionExito() {
        Long id = 1L;
        String usuario = "analista1";
        Solicitud existente = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.BORRADOR)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(existente));
        Mockito.when(solicitudRepository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud resultado = useCase.enviarARevision(id, usuario, "Enviando a revision para aprobacion");

        assertNotNull(resultado);
        assertEquals(EstadoSolicitud.EN_REVISION, resultado.getEstado());
        assertEquals(usuario, resultado.getActualizadaPor());

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("Anular desde BORRADOR transiciona a ANULADA y registra evento auditoria")
    void anularExito() {
        Long id = 1L;
        String usuario = "analista1";
        Solicitud existente = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.BORRADOR)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(existente));
        Mockito.when(solicitudRepository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud resultado = useCase.anular(id, usuario, "Cliente cancelo la solicitud");

        assertNotNull(resultado);
        assertEquals(EstadoSolicitud.ANULADA, resultado.getEstado());
        assertEquals(usuario, resultado.getActualizadaPor());

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("Enviar a revision desde estado distinto de BORRADOR lanza TransicionInvalidaException (HTTP 409)")
    void enviarARevisionEstadoInvalidoLanza409() {
        Long id = 1L;
        Solicitud existente = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.EN_REVISION)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(existente));

        TransicionInvalidaException ex = assertThrows(
                TransicionInvalidaException.class,
                () -> useCase.enviarARevision(id, "analista1", null)
        );

        assertTrue(ex.getMessage().contains("Transición inválida"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }

    @Test
    @DisplayName("Anular desde estado distinto de BORRADOR lanza TransicionInvalidaException (HTTP 409)")
    void anularEstadoInvalidoLanza409() {
        Long id = 1L;
        Solicitud existente = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.APROBADA)
                .build();

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(existente));

        TransicionInvalidaException ex = assertThrows(
                TransicionInvalidaException.class,
                () -> useCase.anular(id, "analista1", null)
        );

        assertTrue(ex.getMessage().contains("Transición inválida"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }

    @Test
    @DisplayName("Transicionar id inexistente lanza RecursoNoEncontradoException (HTTP 404)")
    void transicionarIdInexistenteLanza404() {
        Long idInexistente = 999L;
        Mockito.when(solicitudRepository.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> useCase.enviarARevision(idInexistente, "analista1", null)
        );
    }
}
