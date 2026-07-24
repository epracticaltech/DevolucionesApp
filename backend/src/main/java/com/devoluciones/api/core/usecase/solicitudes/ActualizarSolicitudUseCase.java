package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.exceptions.ReglaNegocioException;
import com.devoluciones.api.core.domain.exceptions.TransicionInvalidaException;
import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.shared.utils.RutUtils;

import java.time.LocalDateTime;

/**
 * Caso de Uso: Actualizar una Solicitud en estado BORRADOR (PUT /api/v1/solicitudes/{id}).
 */
public class ActualizarSolicitudUseCase {

    private final SolicitudRepositoryPort solicitudRepository;

    public ActualizarSolicitudUseCase(SolicitudRepositoryPort solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public Solicitud ejecutar(Long id, Solicitud datosModificados, String usuarioActualizacion) {
        // 1. Buscar la solicitud existente
        Solicitud solicitud = solicitudRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la solicitud con el ID: " + id));

        // 2. Validar que esté en estado BORRADOR (Regla R1 -> HTTP 409 Conflict)
        if (solicitud.getEstado() != EstadoSolicitud.BORRADOR) {
            throw new TransicionInvalidaException(
                    "Solo se pueden editar solicitudes en estado BORRADOR. Estado actual: " + solicitud.getEstado()
            );
        }

        // 3. Validar Idempotencia de referencia de banco (si cambió la referencia y ya existe en otra solicitud)
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
}
