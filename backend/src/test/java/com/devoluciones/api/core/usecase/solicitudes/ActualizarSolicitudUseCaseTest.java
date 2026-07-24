package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.exceptions.TransicionInvalidaException;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ActualizarSolicitudUseCaseTest {

    private SolicitudRepositoryPort solicitudRepository;
    private ActualizarSolicitudUseCase actualizarSolicitudUseCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        actualizarSolicitudUseCase = new ActualizarSolicitudUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Actualizar solicitud en estado BORRADOR edita datos y guarda evento de auditoria")
    void actualizarSolicitudBorradorExito() {
        Long idExistente = 1L;
        String usuario = "analista1";

        Solicitud existente = Solicitud.builder()
                .id(idExistente)
                .folio("DEV-2026-000001")
                .rutCliente("12345678-5")
                .nombreCliente("MARIA PEREZ")
                .monto(new BigDecimal("100000.00"))
                .bancoDestino("BCI")
                .cuentaDestino("123456")
                .referenciaBanco("REF-OLD-123")
                .origen(OrigenSolicitud.MANUAL)
                .estado(EstadoSolicitud.BORRADOR)
                .creadaPor(usuario)
                .build();

        Solicitud datosModificados = Solicitud.builder()
                .rutCliente("12345678-5")
                .nombreCliente("MARIA PEREZ SOTO")
                .monto(new BigDecimal("200000.00"))
                .bancoDestino("BANCO CHILE")
                .cuentaDestino("998877")
                .referenciaBanco("REF-NEW-456")
                .build();

        Mockito.when(solicitudRepository.buscarPorId(idExistente)).thenReturn(Optional.of(existente));
        Mockito.when(solicitudRepository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud resultado = actualizarSolicitudUseCase.ejecutar(idExistente, datosModificados, usuario);

        assertNotNull(resultado);
        assertEquals("MARIA PEREZ SOTO", resultado.getNombreCliente());
        assertEquals(new BigDecimal("200000.00"), resultado.getMonto());
        assertEquals("BANCO CHILE", resultado.getBancoDestino());
        assertEquals("REF-NEW-456", resultado.getReferenciaBanco());
        assertEquals(usuario, resultado.getActualizadaPor());

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("Actualizar solicitud en estado no BORRADOR lanza TransicionInvalidaException (HTTP 409)")
    void actualizarSolicitudEstadoNoBorradorLanzaExcepcion() {
        Long idExistente = 1L;
        Solicitud existente = Solicitud.builder()
                .id(idExistente)
                .estado(EstadoSolicitud.EN_REVISION)
                .build();

        Solicitud datosModificados = Solicitud.builder()
                .rutCliente("12345678-5")
                .nombreCliente("MARIA PEREZ")
                .monto(new BigDecimal("200000.00"))
                .bancoDestino("BANCO CHILE")
                .cuentaDestino("998877")
                .referenciaBanco("REF-NEW-456")
                .build();

        Mockito.when(solicitudRepository.buscarPorId(idExistente)).thenReturn(Optional.of(existente));

        TransicionInvalidaException ex = assertThrows(
                TransicionInvalidaException.class,
                () -> actualizarSolicitudUseCase.ejecutar(idExistente, datosModificados, "analista1")
        );

        assertTrue(ex.getMessage().contains("Solo se pueden editar solicitudes en estado BORRADOR"));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.never()).registrarEvento(any());
    }

    @Test
    @DisplayName("Actualizar solicitud inexistente lanza RecursoNoEncontradoException (HTTP 404)")
    void actualizarSolicitudInexistenteLanzaExcepcion() {
        Long idInexistente = 999L;
        Mockito.when(solicitudRepository.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        Solicitud datosModificados = Solicitud.builder()
                .rutCliente("12345678-5")
                .nombreCliente("MARIA PEREZ")
                .monto(new BigDecimal("200000.00"))
                .bancoDestino("BANCO CHILE")
                .cuentaDestino("998877")
                .referenciaBanco("REF-NEW-456")
                .build();

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> actualizarSolicitudUseCase.ejecutar(idInexistente, datosModificados, "analista1")
        );
    }
}
