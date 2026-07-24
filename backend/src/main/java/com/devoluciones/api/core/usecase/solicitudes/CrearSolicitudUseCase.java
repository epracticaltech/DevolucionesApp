package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.ReglaNegocioException;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.shared.utils.RutUtils;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Random;

/**
 * Caso de Uso: Crear Solicitud Manual (R1, R6 e Idempotencia).
 * Nace siempre en estado BORRADOR con origen MANUAL.
 */
public class CrearSolicitudUseCase {

    private final SolicitudRepositoryPort solicitudRepository;

    public CrearSolicitudUseCase(SolicitudRepositoryPort solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public Solicitud ejecutar(Solicitud solicitud) {
        // 1. Validar Idempotencia si fue provista la referencia bancaria
        if (solicitud.getReferenciaBanco() != null && !solicitud.getReferenciaBanco().isBlank()) {
            if (solicitudRepository.existePorReferenciaBanco(solicitud.getReferenciaBanco())) {
                throw new ReglaNegocioException(
                        "Ya existe una solicitud registrada con la referencia bancaria: "
                                + solicitud.getReferenciaBanco());
            }
        }

        // 2. Generar Folio único garantizado en BD (DEV-AAAA-NNNNNN)
        String folio = generarFolioUnicoEnBD();

        LocalDateTime ahora = LocalDateTime.now();

        // 3. Crear Entidad con valores por defecto del flujo MANUAL
        Solicitud nuevaSolicitud = Solicitud.builder()
                .folio(folio)
                .rutCliente(RutUtils.formatearRut(solicitud.getRutCliente()))
                .nombreCliente(solicitud.getNombreCliente().trim())
                .monto(solicitud.getMonto())
                .moneda("CLP")
                .bancoDestino(solicitud.getBancoDestino())
                .cuentaDestino(solicitud.getCuentaDestino())
                .referenciaBanco(solicitud.getReferenciaBanco())
                .origen(OrigenSolicitud.MANUAL)
                .estado(EstadoSolicitud.BORRADOR)
                .vecesReabierta(0)
                .creadaPor(solicitud.getCreadaPor())
                .fechaCreacion(ahora)
                .actualizadaPor(solicitud.getCreadaPor())
                .fechaActualizacion(ahora)
                .build();

        // 4. Guardar Solicitud en Repositorio
        Solicitud solicitudGuardada = solicitudRepository.guardar(nuevaSolicitud);

        // 5. Registrar evento inicial en auditoría inmutable (R6)
        EventoSolicitud eventoInicial = EventoSolicitud.builder()
                .solicitudId(solicitudGuardada.getId())
                .estadoOrigen(EstadoSolicitud.BORRADOR)
                .estadoDestino(EstadoSolicitud.BORRADOR)
                .usuario(solicitudGuardada.getCreadaPor())
                .fecha(ahora)
                .comentario("Creación inicial de solicitud manual en BORRADOR")
                .build();

        solicitudRepository.registrarEvento(eventoInicial);

        return solicitudGuardada;
    }

    private String generarFolioUnicoEnBD() {
        String folio;
        int anioActual = Year.now().getValue();
        Random random = new Random();

        do {
            int numeroAleatorio = random.nextInt(900000) + 100000;
            folio = String.format("DEV-%d-%06d", anioActual, numeroAleatorio);
        } while (solicitudRepository.buscarPorFolio(folio).isPresent());

        return folio;
    }
}
