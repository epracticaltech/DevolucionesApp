package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.AccesoDenegadoException;
import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.exceptions.ReglaNegocioException;
import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.UsuarioAutenticadoDto;

import java.time.LocalDateTime;

/**
 * Caso de Uso Consolidado: Gestiona las transiciones desde el estado EN_REVISION (Aprobar y Rechazar).
 */
public class GestionarTransicionesRevisionUseCase {

    private final SolicitudRepositoryPort solicitudRepository;

    public GestionarTransicionesRevisionUseCase(SolicitudRepositoryPort solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public Solicitud aprobar(Long id, UsuarioAutenticadoDto usuarioInput, String comentario) {
        // 1. Validar Rol SUPERVISOR (Regla R2 -> HTTP 403 Forbidden)
        validarRolSupervisor(usuarioInput, "aprobar una solicitud");

        // 2. Buscar la solicitud existente
        Solicitud solicitud = buscarSolicitudExistente(id);

        // 3. Validar Separación de Funciones (Regla R7 -> HTTP 400 Bad Request)
        if (solicitud.getCreadaPor() != null && solicitud.getCreadaPor().equalsIgnoreCase(usuarioInput.username())) {
            throw new ReglaNegocioException(
                    "Separación de funciones: El supervisor que aprueba no puede ser el mismo usuario que creó la solicitud (" + usuarioInput.username() + ")."
            );
        }

        EstadoSolicitud estadoOrigen = solicitud.getEstado();

        // 4. Cambiar estado usando Máquina de Estados del Dominio (Regla R1 -> HTTP 409 Conflict si no es EN_REVISION)
        solicitud.cambiarEstado(EstadoSolicitud.APROBADA);
        solicitud.setActualizadaPor(usuarioInput.username());
        solicitud.setFechaActualizacion(LocalDateTime.now());

        // 5. Guardar la solicitud aprobada
        Solicitud guardada = solicitudRepository.guardar(solicitud);

        // 6. Registrar evento de auditoría en la misma transacción (Regla R6)
        String comentarioFinal = (comentario != null && !comentario.isBlank())
                ? comentario.trim()
                : "Solicitud aprobada por el supervisor " + usuarioInput.username();

        EventoSolicitud evento = EventoSolicitud.builder()
                .solicitudId(guardada.getId())
                .estadoOrigen(estadoOrigen)
                .estadoDestino(EstadoSolicitud.APROBADA)
                .usuario(usuarioInput.username())
                .fecha(LocalDateTime.now())
                .comentario(comentarioFinal)
                .build();

        solicitudRepository.registrarEvento(evento);

        return guardada;
    }

    public Solicitud rechazar(Long id, UsuarioAutenticadoDto usuarioInput, String motivoRechazo, String comentario) {
        // 1. Validar Rol SUPERVISOR (Regla R2 -> HTTP 403 Forbidden)
        validarRolSupervisor(usuarioInput, "rechazar una solicitud");

        // 2. Validar Motivo de Rechazo Obligatorio (Regla R3 -> HTTP 400 Bad Request)
        if (motivoRechazo == null || motivoRechazo.isBlank()) {
            throw new ReglaNegocioException("El motivo de rechazo es obligatorio para rechazar una solicitud.");
        }

        // 3. Buscar la solicitud existente
        Solicitud solicitud = buscarSolicitudExistente(id);

        EstadoSolicitud estadoOrigen = solicitud.getEstado();

        // 4. Cambiar estado usando Máquina de Estados del Dominio (Regla R1 -> HTTP 409 Conflict si no es EN_REVISION)
        solicitud.cambiarEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setMotivoRechazo(motivoRechazo.trim());
        solicitud.setActualizadaPor(usuarioInput.username());
        solicitud.setFechaActualizacion(LocalDateTime.now());

        // 5. Guardar la solicitud rechazada
        Solicitud guardada = solicitudRepository.guardar(solicitud);

        // 6. Registrar evento de auditoría en la misma transacción (Regla R6)
        String comentarioFinal = (comentario != null && !comentario.isBlank())
                ? comentario.trim()
                : "Solicitud rechazada. Motivo: " + motivoRechazo.trim();

        EventoSolicitud evento = EventoSolicitud.builder()
                .solicitudId(guardada.getId())
                .estadoOrigen(estadoOrigen)
                .estadoDestino(EstadoSolicitud.RECHAZADA)
                .usuario(usuarioInput.username())
                .fecha(LocalDateTime.now())
                .comentario(comentarioFinal)
                .build();

        solicitudRepository.registrarEvento(evento);

        return guardada;
    }

    private void validarRolSupervisor(UsuarioAutenticadoDto usuarioInput, String accion) {
        if (usuarioInput == null || usuarioInput.rol() == null || !"SUPERVISOR".equalsIgnoreCase(usuarioInput.rol())) {
            throw new AccesoDenegadoException("Acceso denegado: Se requiere rol SUPERVISOR para " + accion + ".");
        }
    }

    private Solicitud buscarSolicitudExistente(Long id) {
        return solicitudRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la solicitud con el ID: " + id));
    }
}
