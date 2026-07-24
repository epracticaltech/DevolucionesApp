package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;

import java.time.LocalDateTime;

/**
 * Caso de Uso: Gestionar transiciones cuyo estado de origen es BORRADOR
 * (POST /api/v1/solicitudes/{id}/enviar y POST /api/v1/solicitudes/{id}/anular).
 */
public class GestionarTransicionesBorradorUseCase {

    private final SolicitudRepositoryPort solicitudRepository;

    public GestionarTransicionesBorradorUseCase(SolicitudRepositoryPort solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public Solicitud enviarARevision(Long id, String usuario, String comentario) {
        return ejecutarTransicion(id, EstadoSolicitud.EN_REVISION, usuario, comentario, "Solicitud enviada a revisión");
    }

    public Solicitud anular(Long id, String usuario, String comentario) {
        return ejecutarTransicion(id, EstadoSolicitud.ANULADA, usuario, comentario, "Solicitud anulada");
    }

    private Solicitud ejecutarTransicion(Long id, EstadoSolicitud estadoDestino, String usuario, String comentarioCustom, String comentarioDefecto) {
        // 1. Buscar la solicitud existente
        Solicitud solicitud = solicitudRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la solicitud con el ID: " + id));

        EstadoSolicitud estadoOrigen = solicitud.getEstado();

        // 2. Cambiar estado usando la Máquina de Estados del Dominio (Lanza TransicionInvalidaException 409 si no es BORRADOR)
        solicitud.cambiarEstado(estadoDestino);
        solicitud.setActualizadaPor(usuario);
        solicitud.setFechaActualizacion(LocalDateTime.now());

        // 3. Guardar la solicitud con el nuevo estado
        Solicitud guardada = solicitudRepository.guardar(solicitud);

        // 4. Registrar el evento de auditoría en la misma transacción (Regla R6)
        String comentarioFinal = (comentarioCustom != null && !comentarioCustom.isBlank())
                ? comentarioCustom.trim()
                : comentarioDefecto + " por el usuario " + usuario;

        EventoSolicitud evento = EventoSolicitud.builder()
                .solicitudId(guardada.getId())
                .estadoOrigen(estadoOrigen)
                .estadoDestino(estadoDestino)
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .comentario(comentarioFinal)
                .build();

        solicitudRepository.registrarEvento(evento);

        return guardada;
    }
}
