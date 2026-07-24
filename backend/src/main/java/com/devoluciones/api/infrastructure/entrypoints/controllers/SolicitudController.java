package com.devoluciones.api.infrastructure.entrypoints.controllers;

import com.devoluciones.api.core.domain.exceptions.ReglaNegocioException;
import com.devoluciones.api.core.domain.models.EventoSolicitud;
import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.Usuario;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import com.devoluciones.api.core.domain.models.pagination.PaginaResultado;
import com.devoluciones.api.core.domain.models.pagination.SolicitudFiltro;
import com.devoluciones.api.core.domain.port.UsuarioRepositoryPort;
import com.devoluciones.api.core.usecase.solicitudes.ConsultarSolicitudesUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarSolicitudesBorradorUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesBorradorUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesFinalesUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesRevisionUseCase;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.AccionSolicitudRequestDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.RechazarSolicitudRequestDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.SolicitudRequestDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.response.EventoSolicitudResponseDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.response.solicitudes.SolicitudBaseDto;
import com.devoluciones.api.infrastructure.entrypoints.dto.response.solicitudes.SolicitudResponseDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.response.usuario.UsuarioDTO;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final GestionarSolicitudesBorradorUseCase gestionarSolicitudesBorradorUseCase;
    private final GestionarTransicionesBorradorUseCase gestionarTransicionesBorradorUseCase;
    private final GestionarTransicionesRevisionUseCase gestionarTransicionesRevisionUseCase;
    private final GestionarTransicionesFinalesUseCase gestionarTransicionesFinalesUseCase;
    private final ConsultarSolicitudesUseCase consultarSolicitudesUseCase;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public SolicitudController(
            GestionarSolicitudesBorradorUseCase gestionarSolicitudesBorradorUseCase,
            GestionarTransicionesBorradorUseCase gestionarTransicionesBorradorUseCase,
            GestionarTransicionesRevisionUseCase gestionarTransicionesRevisionUseCase,
            GestionarTransicionesFinalesUseCase gestionarTransicionesFinalesUseCase,
            ConsultarSolicitudesUseCase consultarSolicitudesUseCase,
            UsuarioRepositoryPort usuarioRepositoryPort) {
        this.gestionarSolicitudesBorradorUseCase = gestionarSolicitudesBorradorUseCase;
        this.gestionarTransicionesBorradorUseCase = gestionarTransicionesBorradorUseCase;
        this.gestionarTransicionesRevisionUseCase = gestionarTransicionesRevisionUseCase;
        this.gestionarTransicionesFinalesUseCase = gestionarTransicionesFinalesUseCase;
        this.consultarSolicitudesUseCase = consultarSolicitudesUseCase;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @PostMapping
    public ResponseEntity<SolicitudBaseDto> crearSolicitud(
            @Valid @RequestBody SolicitudRequestDTO request) {

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();

        Solicitud solicitudInput = Solicitud.builder()
                .rutCliente(request.rutCliente())
                .nombreCliente(request.nombreCliente())
                .monto(request.monto())
                .bancoDestino(request.bancoDestino())
                .cuentaDestino(request.cuentaDestino())
                .referenciaBanco(request.referenciaBanco())
                .creadaPor(usuarioAutenticado.getUsername())
                .build();

        Solicitud creada = gestionarSolicitudesBorradorUseCase.crear(solicitudInput);
        SolicitudBaseDto responseDTO = aSolicitudBaseDto(creada, usuarioAutenticado);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitudBaseDto> actualizarSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudRequestDTO request) {

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();

        Solicitud datosModificados = Solicitud.builder()
                .rutCliente(request.rutCliente())
                .nombreCliente(request.nombreCliente())
                .monto(request.monto())
                .bancoDestino(request.bancoDestino())
                .cuentaDestino(request.cuentaDestino())
                .referenciaBanco(request.referenciaBanco())
                .build();

        Solicitud actualizada = gestionarSolicitudesBorradorUseCase.actualizar(
                id, datosModificados, usuarioAutenticado.getUsername()
        );

        return ResponseEntity.ok(aSolicitudBaseDto(actualizada, usuarioAutenticado));
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<SolicitudBaseDto> enviarARevision(
            @PathVariable Long id,
            @RequestBody(required = false) AccionSolicitudRequestDTO request) {

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        String comentario = request != null ? request.comentario() : null;

        Solicitud enviada = gestionarTransicionesBorradorUseCase.enviarARevision(
                id, usuarioAutenticado.getUsername(), comentario
        );
        return ResponseEntity.ok(aSolicitudBaseDto(enviada, usuarioAutenticado));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<SolicitudBaseDto> anular(
            @PathVariable Long id,
            @RequestBody(required = false) AccionSolicitudRequestDTO request) {

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        String comentario = request != null ? request.comentario() : null;

        Solicitud anulada = gestionarTransicionesBorradorUseCase.anular(
                id, usuarioAutenticado.getUsername(), comentario
        );
        return ResponseEntity.ok(aSolicitudBaseDto(anulada, usuarioAutenticado));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<SolicitudBaseDto> aprobar(
            @PathVariable Long id,
            @RequestBody(required = false) AccionSolicitudRequestDTO request) {

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        String comentario = request != null ? request.comentario() : null;

        Solicitud aprobada = gestionarTransicionesRevisionUseCase.aprobar(
                id, usuarioAutenticado, comentario
        );
        return ResponseEntity.ok(aSolicitudBaseDto(aprobada, usuarioAutenticado));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<SolicitudBaseDto> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazarSolicitudRequestDTO request) {

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();

        Solicitud rechazada = gestionarTransicionesRevisionUseCase.rechazar(
                id, usuarioAutenticado, request.motivoRechazo(), request.comentario()
        );
        return ResponseEntity.ok(aSolicitudBaseDto(rechazada, usuarioAutenticado));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<SolicitudBaseDto> pagar(
            @PathVariable Long id,
            @RequestBody(required = false) AccionSolicitudRequestDTO request) {

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        String comentario = request != null ? request.comentario() : null;

        Solicitud pagada = gestionarTransicionesFinalesUseCase.pagar(
                id, usuarioAutenticado, comentario
        );
        return ResponseEntity.ok(aSolicitudBaseDto(pagada, usuarioAutenticado));
    }

    @PostMapping("/{id}/reabrir")
    public ResponseEntity<SolicitudBaseDto> reabrir(
            @PathVariable Long id,
            @RequestBody(required = false) AccionSolicitudRequestDTO request) {

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        String comentario = request != null ? request.comentario() : null;

        Solicitud reabierta = gestionarTransicionesFinalesUseCase.reabrir(
                id, usuarioAutenticado, comentario
        );
        return ResponseEntity.ok(aSolicitudBaseDto(reabierta, usuarioAutenticado));
    }

    @GetMapping
    public ResponseEntity<PaginaResultado<SolicitudResponseDTO>> listarSolicitudes(
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) String rut,
            @RequestParam(required = false) OrigenSolicitud origen,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        SolicitudFiltro filtro = SolicitudFiltro.builder()
                .estado(estado)
                .rutCliente(rut)
                .origen(origen)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .build();

        PaginaResultado<Solicitud> resultadoDominio = consultarSolicitudesUseCase.listarConFiltros(filtro, page, size);

        List<SolicitudResponseDTO> dtosContent = resultadoDominio.getContenido().stream()
                .map(s -> aResponseDTO(s))
                .toList();

        PaginaResultado<SolicitudResponseDTO> paginaResponse = new PaginaResultado<>(
                dtosContent,
                resultadoDominio.getPagina(),
                resultadoDominio.getTamano(),
                resultadoDominio.getTotalElementos(),
                resultadoDominio.getTotalPaginas()
        );

        return ResponseEntity.ok(paginaResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudBaseDto> obtenerPorId(@PathVariable Long id) {
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        Solicitud solicitud = consultarSolicitudesUseCase.obtenerPorId(id);
        return ResponseEntity.ok(aSolicitudBaseDto(solicitud, usuarioAutenticado));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<EventoSolicitudResponseDTO>> obtenerHistorial(@PathVariable Long id) {
        List<EventoSolicitud> eventos = consultarSolicitudesUseCase.obtenerHistorial(id);
        List<EventoSolicitudResponseDTO> dtos = eventos.stream()
                .map(this::aEventoSolicitudResponseDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return usuarioRepositoryPort.buscarPorUsername(username)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontró el usuario que está realizando la solicitud (" + username + "). Debe autenticarse nuevamente."
                ));
    }

    private SolicitudBaseDto aSolicitudBaseDto(Solicitud s, Usuario u) {
        SolicitudResponseDTO solicitudDTO = aResponseDTO(s);
        UsuarioDTO usuarioDTO = new UsuarioDTO(u.getUsername(), u.getRol(), u.getMail());
        return new SolicitudBaseDto(solicitudDTO, usuarioDTO);
    }

    public SolicitudResponseDTO aResponseDTO(Solicitud s) {
        return new SolicitudResponseDTO(
                s.getId(),
                s.getFolio(),
                s.getRutCliente(),
                s.getNombreCliente(),
                s.getMonto(),
                s.getMoneda(),
                s.getBancoDestino(),
                s.getCuentaDestino(),
                s.getReferenciaBanco(),
                s.getOrigen(),
                s.getEstado(),
                s.getMotivoRechazo(),
                s.getVecesReabierta(),
                s.getCreadaPor(),
                s.getFechaCreacion(),
                s.getActualizadaPor(),
                s.getFechaActualizacion()
        );
    }

    private EventoSolicitudResponseDTO aEventoSolicitudResponseDTO(EventoSolicitud e) {
        return new EventoSolicitudResponseDTO(
                e.getId(),
                e.getSolicitudId(),
                e.getEstadoOrigen(),
                e.getEstadoDestino(),
                e.getUsuario(),
                e.getFecha(),
                e.getComentario()
        );
    }
}
