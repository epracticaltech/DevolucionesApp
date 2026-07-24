package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
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

class ObtenerSolicitudPorIdUseCaseTest {

    private SolicitudRepositoryPort solicitudRepository;
    private ObtenerSolicitudPorIdUseCase obtenerSolicitudPorIdUseCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        obtenerSolicitudPorIdUseCase = new ObtenerSolicitudPorIdUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Obtener Solicitud por ID existente retorna la Solicitud correctamente")
    void obtenerSolicitudPorIdExistenteExito() {
        Long idExistente = 1L;
        Solicitud solicitudEsperada = Solicitud.builder()
                .id(idExistente)
                .folio("DEV-2026-000001")
                .rutCliente("12345678-5")
                .nombreCliente("MARIA PEREZ")
                .monto(new BigDecimal("150000.00"))
                .moneda("CLP")
                .bancoDestino("BANCO CHILE")
                .cuentaDestino("00123456")
                .origen(OrigenSolicitud.MANUAL)
                .estado(EstadoSolicitud.BORRADOR)
                .creadaPor("analista1")
                .build();

        Mockito.when(solicitudRepository.buscarPorId(idExistente))
                .thenReturn(Optional.of(solicitudEsperada));

        Solicitud resultado = obtenerSolicitudPorIdUseCase.ejecutar(idExistente);

        assertNotNull(resultado);
        assertEquals(idExistente, resultado.getId());
        assertEquals("DEV-2026-000001", resultado.getFolio());
        assertEquals(EstadoSolicitud.BORRADOR, resultado.getEstado());

        Mockito.verify(solicitudRepository, Mockito.times(1)).buscarPorId(idExistente);
    }

    @Test
    @DisplayName("Obtener Solicitud por ID inexistente lanza RecursoNoEncontradoException")
    void obtenerSolicitudPorIdInexistenteLanzaExcepcion() {
        Long idInexistente = 999L;

        Mockito.when(solicitudRepository.buscarPorId(idInexistente))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> obtenerSolicitudPorIdUseCase.ejecutar(idInexistente)
        );

        assertTrue(ex.getMessage().contains("No se encontró la solicitud con el ID: 999"));
        Mockito.verify(solicitudRepository, Mockito.times(1)).buscarPorId(idInexistente);
    }
}
