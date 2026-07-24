package com.devoluciones.api.core.usecase.cargas;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.cargas.CargaMasiva;
import com.devoluciones.api.core.domain.models.cargas.DetalleCargaError;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import com.devoluciones.api.core.domain.port.CargaMasivaRepositoryPort;
import com.devoluciones.api.core.domain.port.FolioGeneratorPort;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.shared.utils.RutUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Caso de Uso: Procesar archivos CSV de Carga Masiva de solicitudes (POST /api/v1/cargas)
 * y consultar el resumen por ID (GET /api/v1/cargas/{id}).
 */
public class ProcesarCargaMasivaUseCase {

    private static final int BATCH_SIZE = 100;

    private final CargaMasivaRepositoryPort cargaMasivaRepository;
    private final SolicitudRepositoryPort solicitudRepository;
    private final FolioGeneratorPort folioGenerator;

    @PersistenceContext
    private EntityManager entityManager;

    public ProcesarCargaMasivaUseCase(
            CargaMasivaRepositoryPort cargaMasivaRepository,
            SolicitudRepositoryPort solicitudRepository,
            FolioGeneratorPort folioGenerator) {
        this.cargaMasivaRepository = cargaMasivaRepository;
        this.solicitudRepository = solicitudRepository;
        this.folioGenerator = folioGenerator;
    }

    public CargaMasiva procesarArchivoCsv(String nombreArchivo, InputStream inputStream) {
        LocalDateTime ahora = LocalDateTime.now();

        // 1. Guardar Registro Inicial de Carga Masiva (estado="PROCESANDO")
        CargaMasiva carga = CargaMasiva.builder()
                .nombreArchivo(nombreArchivo)
                .totalFilas(0)
                .filasProcesadas(0)
                .filasRechazadas(0)
                .estado("PROCESANDO")
                .fechaCarga(ahora)
                .build();

        CargaMasiva cargaGuardada = cargaMasivaRepository.guardar(carga);
        Long cargaId = cargaGuardada.getId();

        List<DetalleCargaError> listaErrores = new ArrayList<>();
        Set<String> referenciasVistasEnArchivo = new HashSet<>();

        List<Solicitud> chunkSolicitudes = new ArrayList<>(BATCH_SIZE);
        List<EventoSolicitud> chunkEventos = new ArrayList<>(BATCH_SIZE);

        int numeroFila = 0;
        int filasProcesadasCount = 0;
        int filasRechazadasCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                numeroFila++;

                if (linea.isBlank()) {
                    continue;
                }

                // Descartar encabezado CSV si la primera línea contiene 'rut'
                if (numeroFila == 1 && linea.toLowerCase().contains("rut")) {
                    continue;
                }

                String[] columnas = linea.split(";");
                if (columnas.length < 6) {
                    filasRechazadasCount++;
                    listaErrores.add(new DetalleCargaError(null, cargaId, numeroFila, "estructura",
                            "Fila incompleta: se esperaban 6 campos separados por punto y coma ';'"));
                    continue;
                }

                String rutCliente = columnas[0].trim();
                String nombreCliente = columnas[1].trim();
                String montoStr = columnas[2].trim();
                String bancoDestino = columnas[3].trim();
                String cuentaDestino = columnas[4].trim();
                String referenciaBanco = columnas[5].trim();

                // 2. Validaciones por Fila
                if (rutCliente.isBlank() || !RutUtils.esRutValido(rutCliente)) {
                    filasRechazadasCount++;
                    listaErrores.add(new DetalleCargaError(null, cargaId, numeroFila, "rut_cliente",
                            "RUT del cliente inválido o con dígito verificador erróneo: " + rutCliente));
                    continue;
                }

                if (nombreCliente.isBlank()) {
                    filasRechazadasCount++;
                    listaErrores.add(new DetalleCargaError(null, cargaId, numeroFila, "nombre_cliente",
                            "El nombre del cliente es obligatorio"));
                    continue;
                }

                BigDecimal monto;
                try {
                    monto = new BigDecimal(montoStr);
                    if (monto.compareTo(BigDecimal.ZERO) <= 0 || monto.compareTo(new BigDecimal("10000000.00")) > 0) {
                        filasRechazadasCount++;
                        listaErrores.add(new DetalleCargaError(null, cargaId, numeroFila, "monto",
                                "El monto debe ser positivo y no superar 10.000.000 CLP: " + montoStr));
                        continue;
                    }
                } catch (Exception ex) {
                    filasRechazadasCount++;
                    listaErrores.add(new DetalleCargaError(null, cargaId, numeroFila, "monto",
                            "El monto no es un formato numérico válido: " + montoStr));
                    continue;
                }

                if (bancoDestino.isBlank()) {
                    filasRechazadasCount++;
                    listaErrores.add(new DetalleCargaError(null, cargaId, numeroFila, "banco_destino",
                            "El banco destino es obligatorio"));
                    continue;
                }

                if (cuentaDestino.isBlank()) {
                    filasRechazadasCount++;
                    listaErrores.add(new DetalleCargaError(null, cargaId, numeroFila, "cuenta_destino",
                            "La cuenta destino es obligatoria"));
                    continue;
                }

                if (referenciaBanco.isBlank()) {
                    filasRechazadasCount++;
                    listaErrores.add(new DetalleCargaError(null, cargaId, numeroFila, "referencia_banco",
                            "La referencia bancaria es obligatoria"));
                    continue;
                }

                // Validar Idempotencia (Referencia duplicada en BD o en el mismo archivo CSV)
                String refLower = referenciaBanco.toLowerCase();
                if (referenciasVistasEnArchivo.contains(refLower) || solicitudRepository.existePorReferenciaBanco(referenciaBanco)) {
                    filasRechazadasCount++;
                    listaErrores.add(new DetalleCargaError(null, cargaId, numeroFila, "referencia_banco",
                            "Referencia bancaria duplicada (idempotencia): " + referenciaBanco));
                    continue;
                }

                // 3. Crear Objeto Solicitud en estado EN_REVISION (origen CARGA_MASIVA)
                String folio = folioGenerator.generarFolio();

                Solicitud solicitud = Solicitud.builder()
                        .folio(folio)
                        .rutCliente(RutUtils.formatearRut(rutCliente))
                        .nombreCliente(nombreCliente.toUpperCase())
                        .monto(monto)
                        .moneda("CLP")
                        .bancoDestino(bancoDestino.toUpperCase())
                        .cuentaDestino(cuentaDestino)
                        .referenciaBanco(referenciaBanco)
                        .origen(OrigenSolicitud.CARGA_MASIVA)
                        .estado(EstadoSolicitud.EN_REVISION)
                        .vecesReabierta(0)
                        .creadaPor("SISTEMA_CARGA_MASIVA")
                        .fechaCreacion(ahora)
                        .actualizadaPor("SISTEMA_CARGA_MASIVA")
                        .fechaActualizacion(ahora)
                        .build();

                chunkSolicitudes.add(solicitud);
                referenciasVistasEnArchivo.add(refLower);
                filasProcesadasCount++;

                // 4. Procesar Lote por Chunks de 100 Elementos
                if (chunkSolicitudes.size() == BATCH_SIZE) {
                    procesarChunkLote(chunkSolicitudes, chunkEventos, nombreArchivo, ahora);
                }
            }

            // Procesar último chunk remanente si quedan elementos sin guardar
            if (!chunkSolicitudes.isEmpty()) {
                procesarChunkLote(chunkSolicitudes, chunkEventos, nombreArchivo, ahora);
            }

        } catch (Exception e) {
            cargaGuardada.setEstado("ERROR");
            cargaMasivaRepository.guardar(cargaGuardada);
            throw new RuntimeException("Error durante el procesamiento del archivo CSV: " + e.getMessage(), e);
        }

        // 5. Guardar Errores y Actualizar Resumen Final de Carga Masiva (estado="COMPLETADO")
        cargaMasivaRepository.guardarErrores(listaErrores);

        cargaGuardada.setTotalFilas(numeroFila);
        cargaGuardada.setFilasProcesadas(filasProcesadasCount);
        cargaGuardada.setFilasRechazadas(filasRechazadasCount);
        cargaGuardada.setEstado("COMPLETADO");
        cargaGuardada.setErrores(listaErrores);

        return cargaMasivaRepository.guardar(cargaGuardada);
    }

    private void procesarChunkLote(
            List<Solicitud> chunkSolicitudes,
            List<EventoSolicitud> chunkEventos,
            String nombreArchivo,
            LocalDateTime ahora) {

        // A. Persistir Lote de Solicitudes vía Repositorio
        List<Solicitud> solicitudesGuardadas = solicitudRepository.guardarTodas(new ArrayList<>(chunkSolicitudes));

        // B. Generar Eventos de Auditoría (EN_REVISION -> EN_REVISION) con los IDs asignados
        for (Solicitud s : solicitudesGuardadas) {
            EventoSolicitud eventoInicial = EventoSolicitud.builder()
                    .solicitudId(s.getId())
                    .estadoOrigen(EstadoSolicitud.EN_REVISION)
                    .estadoDestino(EstadoSolicitud.EN_REVISION)
                    .usuario("SISTEMA_CARGA_MASIVA")
                    .fecha(ahora)
                    .comentario("Creación automática desde Carga Masiva (Archivo: " + nombreArchivo + ")")
                    .build();
            chunkEventos.add(eventoInicial);
        }

        // C. Persistir Lote de Eventos vía Repositorio
        solicitudRepository.registrarEventos(new ArrayList<>(chunkEventos));

        // D. Flush & Clear memoria RAM del Persistence Context de JPA
        if (entityManager != null) {
            entityManager.flush();
            entityManager.clear();
        }

        chunkSolicitudes.clear();
        chunkEventos.clear();
    }

    public CargaMasiva obtenerResumenCarga(Long cargaId) {
        return cargaMasivaRepository.buscarPorId(cargaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el registro de carga masiva con el ID: " + cargaId));
    }
}
