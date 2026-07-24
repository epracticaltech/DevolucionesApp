package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.exceptions.ReglaNegocioException;
import com.devoluciones.api.core.domain.exceptions.TransicionInvalidaException;
import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.shared.utils.RutUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Caso de Uso Consolidado: Gestiona la creación y actualización de solicitudes en estado BORRADOR.
 */
public class GestionarSolicitudesBorradorUseCase {

    private final SolicitudRepositoryPort solicitudRepository;

    public GestionarSolicitudesBorradorUseCase(SolicitudRepositoryPort solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public Solicitud crear(Solicitud input) {
        // 1. Validar Idempotencia de Referencia Bancaria
        if (input.getReferenciaBanco() != null && solicitudRepository.existePorReferenciaBanco(input.getReferenciaBanco())) {
            throw new ReglaNegocioException(
                    "Ya existe una solicitud registrada con la referencia bancaria: " + input.getReferenciaBanco()
            );
        }

        // 2. Generar Folio Único (DEV-AAAA-NNNNNN)
        String folio;
        do {
            folio = generarFolioUnico();
        } while (solicitudRepository.buscarPorFolio(folio).isPresent());

        LocalDateTime ahora = LocalDateTime.now();

        // 3. Construir Objeto de Dominio Solicitud
        Solicitud nuevaSolicitud = Solicitud.builder()
                .folio(folio)
                .rutCliente(RutUtils.formatearRut(input.getRutCliente()))
                .nombreCliente(input.getNombreCliente().trim().toUpperCase())
                .monto(input.getMonto())
                .moneda("CLP")
                .bancoDestino(input.getBancoDestino().trim().toUpperCase())
                .cuentaDestino(input.getCuentaDestino().trim())
                .referenciaBanco(input.getReferenciaBanco().trim())
                .origen(OrigenSolicitud.MANUAL)
                .estado(EstadoSolicitud.BORRADOR)
                .vecesReabierta(0)
                .creadaPor(input.getCreadaPor())
                .fechaCreacion(ahora)
                .actualizadaPor(input.getCreadaPor())
                .fechaActualizacion(ahora)
                .build();

        // 4. Guardar Solicitud
        Solicitud guardada = solicitudRepository.guardar(nuevaSolicitud);

        // 5. Registrar Evento de Auditoría (Regla R6)
        EventoSolicitud eventoInicial = EventoSolicitud.builder()
                .solicitudId(guardada.getId())
                .estadoOrigen(null)
                .estadoDestino(EstadoSolicitud.BORRADOR)
                .usuario(guardada.getCreadaPor())
                .fecha(ahora)
                .comentario("Creación manual de solicitud de devolución")
                .build();

        solicitudRepository.registrarEvento(eventoInicial);

        return guardada;
    }

    public Solicitud actualizar(Long id, Solicitud datosModificados, String usuarioActualizacion) {
        // 1. Buscar la solicitud existente
        Solicitud solicitud = solicitudRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la solicitud con el ID: " + id));

        // 2. Validar que esté en estado BORRADOR (Regla R1 -> HTTP 409 Conflict)
        if (solicitud.getEstado() != EstadoSolicitud.BORRADOR) {
            throw new TransicionInvalidaException(
                    "Solo se pueden editar solicitudes en estado BORRADOR. Estado actual: " + solicitud.getEstado()
            );
        }

        // 3. Validar Idempotencia de referencia bancaria si cambió
        if (datosModificados.getReferenciaBanco() != null
                && !datosModificados.getReferenciaBanco().equalsIgnoreCase(solicitud.getReferenciaBanco())
                && solicitudRepository.existePorReferenciaBanco(datosModificados.getReferenciaBanco())) {
            throw new ReglaNegocioException(
                    "Ya existe una solicitud registrada con la referencia bancaria: " + datosModificados.getReferenciaBanco()
            );
        }

        // 4. Actualizar campos editables
        solicitud.setRutCliente(RutUtils.formatearRut(datosModificados.getRutCliente()));
        solicitud.setNombreCliente(datosModificados.getNombreCliente().trim().toUpperCase());
        solicitud.setMonto(datosModificados.getMonto());
        solicitud.setBancoDestino(datosModificados.getBancoDestino().trim().toUpperCase());
        solicitud.setCuentaDestino(datosModificados.getCuentaDestino().trim());
        solicitud.setReferenciaBanco(datosModificados.getReferenciaBanco().trim());
        solicitud.setActualizadaPor(usuarioActualizacion);
        solicitud.setFechaActualizacion(LocalDateTime.now());

        // 5. Guardar la solicitud modificada
        Solicitud actualizada = solicitudRepository.guardar(solicitud);

        // 6. Registrar evento de auditoría (Regla R6)
        EventoSolicitud evento = EventoSolicitud.builder()
                .solicitudId(actualizada.getId())
                .estadoOrigen(EstadoSolicitud.BORRADOR)
                .estadoDestino(EstadoSolicitud.BORRADOR)
                .usuario(usuarioActualizacion)
                .fecha(LocalDateTime.now())
                .comentario("Solicitud editada en estado BORRADOR por el usuario " + usuarioActualizacion)
                .build();

        solicitudRepository.registrarEvento(evento);

        return actualizada;
    }

    private String generarFolioUnico() {
        int anioActual = LocalDateTime.now().getYear();
        int secuencial = ThreadLocalRandom.current().nextInt(1, 999999);
        return String.format("DEV-%d-%06d", anioActual, secuencial);
    }
}
