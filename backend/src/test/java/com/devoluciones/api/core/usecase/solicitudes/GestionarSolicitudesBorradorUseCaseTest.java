package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.ReglaNegocioException;
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

class GestionarSolicitudesBorradorUseCaseTest {

    private SolicitudRepositoryPort solicitudRepository;
    private GestionarSolicitudesBorradorUseCase useCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        useCase = new GestionarSolicitudesBorradorUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Crear solicitud manual exitosa genera folio, asigna estado BORRADOR y guarda evento auditoria")
    void crearSolicitudManualExito() {
        Solicitud input = Solicitud.builder()
                .rutCliente("12345678-5")
                .nombreCliente("MARIA PEREZ")
                .monto(new BigDecimal("150000.00"))
                .bancoDestino("BANCO CHILE")
                .cuentaDestino("00123456")
                .referenciaBanco("REF-2026-000123")
                .creadaPor("analista1")
                .build();

        Mockito.when(solicitudRepository.existePorReferenciaBanco("REF-2026-000123")).thenReturn(false);
        Mockito.when(solicitudRepository.buscarPorFolio(any())).thenReturn(Optional.empty());
        Mockito.when(solicitudRepository.guardar(any())).thenAnswer(invocation -> {
            Solicitud s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        Solicitud creada = useCase.crear(input);

        assertNotNull(creada);
        assertEquals(1L, creada.getId());
        assertEquals(EstadoSolicitud.BORRADOR, creada.getEstado());
        assertEquals(OrigenSolicitud.MANUAL, creada.getOrigen());
        assertTrue(creada.getFolio().startsWith("DEV-"));

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("Crear solicitud con referencia duplicada lanza ReglaNegocioException")
    void crearSolicitudReferenciaDuplicadaLanzaExcepcion() {
        Solicitud input = Solicitud.builder()
                .rutCliente("12345678-5")
                .referenciaBanco("REF-DUP-123")
                .build();

        Mockito.when(solicitudRepository.existePorReferenciaBanco("REF-DUP-123")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> useCase.crear(input));
        Mockito.verify(solicitudRepository, Mockito.never()).guardar(any());
    }

    @Test
    @DisplayName("Actualizar solicitud en estado BORRADOR edita datos y guarda evento auditoria")
    void actualizarSolicitudBorradorExito() {
        Long id = 1L;
        String usuario = "analista1";

        Solicitud existente = Solicitud.builder()
                .id(id)
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

        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(existente));
        Mockito.when(solicitudRepository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud resultado = useCase.actualizar(id, datosModificados, usuario);

        assertNotNull(resultado);
        assertEquals("MARIA PEREZ SOTO", resultado.getNombreCliente());
        assertEquals(new BigDecimal("200000.00"), resultado.getMonto());
        assertEquals(usuario, resultado.getActualizadaPor());

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("Actualizar solicitud en estado no BORRADOR lanza TransicionInvalidaException (HTTP 409)")
    void actualizarSolicitudEstadoNoBorradorLanzaExcepcion() {
        Long id = 1L;
        Solicitud existente = Solicitud.builder()
                .id(id)
                .estado(EstadoSolicitud.EN_REVISION)
                .build();

        Solicitud datosModificados = Solicitud.builder().build();
        Mockito.when(solicitudRepository.buscarPorId(id)).thenReturn(Optional.of(existente));

        assertThrows(TransicionInvalidaException.class, () -> useCase.actualizar(id, datosModificados, "analista1"));
    }
}
