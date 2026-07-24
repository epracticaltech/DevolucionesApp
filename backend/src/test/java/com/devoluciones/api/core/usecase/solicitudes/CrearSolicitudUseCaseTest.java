package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.ReglaNegocioException;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class CrearSolicitudUseCaseTest {

    private SolicitudRepositoryPort solicitudRepository;
    private CrearSolicitudUseCase crearSolicitudUseCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        crearSolicitudUseCase = new CrearSolicitudUseCase(solicitudRepository);
    }

    @Test
    @DisplayName("Crear Solicitud Manual Válida nace en estado BORRADOR con Folio autogenerado")
    void crearSolicitudManualValidaExito() {
        Solicitud solicitudInput = Solicitud.builder()
                .rutCliente("12345678-5")
                .nombreCliente("MARIA PEREZ")
                .monto(new BigDecimal("150000.00"))
                .bancoDestino("BANCO CHILE")
                .cuentaDestino("00123456")
                .creadaPor("analista1")
                .build();

        Mockito.when(solicitudRepository.guardar(any(Solicitud.class))).thenAnswer(invocation -> {
            Solicitud s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        Solicitud creada = crearSolicitudUseCase.ejecutar(solicitudInput);

        assertNotNull(creada);
        assertEquals(1L, creada.getId());
        assertEquals(EstadoSolicitud.BORRADOR, creada.getEstado());
        assertEquals("12345678-5", creada.getRutCliente());
        assertEquals("analista1", creada.getCreadaPor());
        assertNotNull(creada.getFolio());
        assertTrue(creada.getFolio().startsWith("DEV-"));

        Mockito.verify(solicitudRepository, Mockito.times(1)).guardar(any());
        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEvento(any());
    }

    @Test
    @DisplayName("Idempotencia: Referencia bancaria duplicada lanza ReglaNegocioException")
    void crearSolicitudReferenciaDuplicadaLanzaExcepcion() {
        String refDuplicada = "REF-2026-000123";

        Solicitud solicitudInput = Solicitud.builder()
                .rutCliente("12345678-5")
                .nombreCliente("MARIA PEREZ")
                .monto(new BigDecimal("100000.00"))
                .bancoDestino("BCI")
                .cuentaDestino("998877")
                .referenciaBanco(refDuplicada)
                .creadaPor("analista1")
                .build();

        Mockito.when(solicitudRepository.existePorReferenciaBanco(refDuplicada)).thenReturn(true);

        ReglaNegocioException ex = assertThrows(
                ReglaNegocioException.class,
                () -> crearSolicitudUseCase.ejecutar(solicitudInput));

        assertTrue(ex.getMessage().contains("Ya existe una solicitud registrada con la referencia bancaria"));
    }
}
