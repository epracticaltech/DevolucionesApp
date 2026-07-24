package com.devoluciones.api.core.usecase.cargas;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.cargas.CargaMasiva;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import com.devoluciones.api.core.domain.port.CargaMasivaRepositoryPort;
import com.devoluciones.api.core.domain.port.FolioGeneratorPort;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ProcesarCargaMasivaUseCaseTest {

    private CargaMasivaRepositoryPort cargaMasivaRepository;
    private SolicitudRepositoryPort solicitudRepository;
    private FolioGeneratorPort folioGenerator;
    private ProcesarCargaMasivaUseCase useCase;

    @BeforeEach
    void setUp() {
        cargaMasivaRepository = Mockito.mock(CargaMasivaRepositoryPort.class);
        solicitudRepository = Mockito.mock(SolicitudRepositoryPort.class);
        folioGenerator = Mockito.mock(FolioGeneratorPort.class);

        useCase = new ProcesarCargaMasivaUseCase(cargaMasivaRepository, solicitudRepository, folioGenerator);

        Mockito.when(folioGenerator.generarFolio()).thenReturn("DEV-2026-000101");
        Mockito.when(cargaMasivaRepository.guardar(any())).thenAnswer(invocation -> {
            CargaMasiva c = invocation.getArgument(0);
            if (c.getId() == null) {
                c.setId(1L);
            }
            return c;
        });

        AtomicLong idSeq = new AtomicLong(10L);
        Mockito.when(solicitudRepository.guardarTodas(any())).thenAnswer(invocation -> {
            List<Solicitud> lista = invocation.getArgument(0);
            for (Solicitud s : lista) {
                if (s.getId() == null) {
                    s.setId(idSeq.getAndIncrement());
                }
            }
            return lista;
        });
    }

    @Test
    @DisplayName("Procesar CSV válido crea solicitudes en EN_REVISION con origen CARGA_MASIVA y evento de auditoria")
    void procesarCsvValidoExito() {
        // RUTs válidos: 12345678-5 y 11111111-1
        String csvContent = "rut_cliente;nombre_cliente;monto;banco_destino;cuenta_destino;referencia_banco\n" +
                "12345678-5;MARIA PEREZ;150000.00;BANCO CHILE;0012345678;REF-2026-001\n" +
                "11111111-1;JUAN SOTO;250000.00;BCI;99887766;REF-2026-002\n";

        InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        Mockito.when(solicitudRepository.existePorReferenciaBanco(any())).thenReturn(false);

        CargaMasiva resultado = useCase.procesarArchivoCsv("pagos.csv", is);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("COMPLETADO", resultado.getEstado());
        assertEquals(2, resultado.getFilasProcesadas());
        assertEquals(0, resultado.getFilasRechazadas());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Solicitud>> solicitudCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(solicitudRepository, Mockito.times(1)).guardarTodas(solicitudCaptor.capture());

        List<Solicitud> loteGuardado = solicitudCaptor.getValue();
        assertEquals(2, loteGuardado.size());

        Solicitud primeraGuardada = loteGuardado.get(0);
        assertEquals(EstadoSolicitud.EN_REVISION, primeraGuardada.getEstado());
        assertEquals(OrigenSolicitud.CARGA_MASIVA, primeraGuardada.getOrigen());

        Mockito.verify(solicitudRepository, Mockito.times(1)).registrarEventos(any());
    }

    @Test
    @DisplayName("Procesar CSV con filas defectuosas y duplicados (tolerancia a errores) registra errores sin abortar la carga")
    void procesarCsvConErroresTolerancia() {
        // RUT 12345678-5 (válido), 11111111-9 (RUT inválido), 11111111-1 (monto negativo)
        String csvContent = "rut_cliente;nombre_cliente;monto;banco_destino;cuenta_destino;referencia_banco\n" +
                "12345678-5;MARIA PEREZ;150000.00;BANCO CHILE;0012345678;REF-OK-001\n" +
                "11111111-9;CARLOS BAD;150000.00;BANCO CHILE;0012345678;REF-BAD-002\n" + // RUT inválido (DV debería ser 1)
                "11111111-1;JUAN SOTO;-500.00;BCI;99887766;REF-BAD-003\n" + // Monto negativo
                "12345678-5;PEDRO DUP;50000.00;BANCO CHILE;0012345678;REF-OK-001\n"; // Referencia duplicada en el mismo archivo

        InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        Mockito.when(solicitudRepository.existePorReferenciaBanco("REF-OK-001")).thenReturn(false);

        CargaMasiva resultado = useCase.procesarArchivoCsv("pagos_mixtos.csv", is);

        assertNotNull(resultado);
        assertEquals("COMPLETADO", resultado.getEstado());
        assertEquals(1, resultado.getFilasProcesadas());
        assertEquals(3, resultado.getFilasRechazadas());
        assertEquals(3, resultado.getErrores().size());
    }

    @Test
    @DisplayName("Obtener resumen de carga por ID existente retorna CargaMasiva")
    void obtenerResumenCargaExito() {
        Long id = 1L;
        CargaMasiva mockCarga = CargaMasiva.builder().id(id).estado("COMPLETADO").totalFilas(10).build();
        Mockito.when(cargaMasivaRepository.buscarPorId(id)).thenReturn(Optional.of(mockCarga));

        CargaMasiva resultado = useCase.obtenerResumenCarga(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    @DisplayName("Obtener resumen de carga inexistente lanza RecursoNoEncontradoException (HTTP 404)")
    void obtenerResumenCargaInexistenteLanza404() {
        Long id = 999L;
        Mockito.when(cargaMasivaRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtenerResumenCarga(id));
    }
}
