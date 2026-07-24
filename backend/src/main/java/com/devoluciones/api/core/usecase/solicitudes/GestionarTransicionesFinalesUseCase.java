package com.devoluciones.api.core.usecase.solicitudes;

import com.devoluciones.api.core.domain.exceptions.RecursoNoEncontradoException;
import com.devoluciones.api.core.domain.exceptions.TransicionInvalidaException;
import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.Usuario;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.port.SolicitudRepositoryPort;

import java.time.LocalDateTime;

/**
 * Caso de Uso Consolidado: Gestiona las transiciones finales (Pagar y Reabrir).
 */
public class GestionarTransicionesFinalesUseCase {

    private final SolicitudRepositoryPort solicitudRepository;

    public GestionarTransicionesFinalesUseCase(SolicitudRepositoryPort solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public Solicitud pagar(Long id, Usuario usuario, String comentario) {
        // 1. Buscar la solicitud existente
        Solicitud solicitud = buscarSolicitudExistente(id);

        EstadoSolicitud estadoOrigen = solicitud.getEstado();

        // 2. Cambiar estado usando Máquina de Estados del Dominio (Regla R1 -> HTTP 409 Conflict si no es APROBADA)
        solicitud.cambiarEstado(EstadoSolicitud.PAGADA);
        solicitud.setActualizadaPor(usuario.getUsername());
        solicitud.setFechaActualizacion(LocalDateTime.now());

        // 3. Guardar la solicitud pagada
        Solicitud guardada = solicitudRepository.guardar(solicitud);

        // 4. Registrar evento de auditoría en la misma transacción (Regla R6)
        String comentarioFinal = (comentario != null && !comentario.isBlank())
                ? comentario.trim()
                : "Solicitud pagada por el supervisor " + usuario.getUsername();

        EventoSolicitud evento = EventoSolicitud.builder()
                .solicitudId(guardada.getId())
                .estadoOrigen(estadoOrigen)
                .estadoDestino(EstadoSolicitud.PAGADA)
                .usuario(usuario.getUsername())
                .fecha(LocalDateTime.now())
                .comentario(comentarioFinal)
                .build();

        solicitudRepository.registrarEvento(evento);

        return guardada;
    }

    public Solicitud reabrir(Long id, Usuario usuario, String comentario) {
        // 1. Buscar la solicitud existente
        Solicitud solicitud = buscarSolicitudExistente(id);

        // 2. Validar Límite de Reapertura (Regla R4 -> Máximo 1 vez -> HTTP 409 Conflict)
        if (solicitud.getVecesReabierta() != null && solicitud.getVecesReabierta() >= 1) {
            throw new TransicionInvalidaException(
                    "La solicitud de devolución ya ha sido reabierta el número máximo de veces permitido (1 vez)."
            );
        }

        EstadoSolicitud estadoOrigen = solicitud.getEstado();

        // 3. Cambiar estado usando Máquina de Estados del Dominio (Regla R1 -> HTTP 409 Conflict si no es RECHAZADA)
        solicitud.cambiarEstado(EstadoSolicitud.BORRADOR);
        solicitud.setVecesReabierta((solicitud.getVecesReabierta() == null ? 0 : solicitud.getVecesReabierta()) + 1);
        solicitud.setActualizadaPor(usuario.getUsername());
        solicitud.setFechaActualizacion(LocalDateTime.now());

        // 4. Guardar la solicitud reabierta
        Solicitud guardada = solicitudRepository.guardar(solicitud);

        // 5. Registrar evento de auditoría en la misma transacción (Regla R6)
        String comentarioFinal = (comentario != null && !comentario.isBlank())
                ? comentario.trim()
                : "Solicitud reabierta a BORRADOR por el usuario " + usuario.getUsername();

        EventoSolicitud evento = EventoSolicitud.builder()
                .solicitudId(guardada.getId())
                .estadoOrigen(estadoOrigen)
                .estadoDestino(EstadoSolicitud.BORRADOR)
                .usuario(usuario.getUsername())
                .fecha(LocalDateTime.now())
                .comentario(comentarioFinal)
                .build();

        solicitudRepository.registrarEvento(evento);

        return guardada;
    }

    private Solicitud buscarSolicitudExistente(Long id) {
        return solicitudRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la solicitud con el ID: " + id));
    }
}
