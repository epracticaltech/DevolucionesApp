package com.devoluciones.api.infrastructure.entrypoints.controllers;

import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import com.devoluciones.api.core.domain.models.pagination.PaginaResultado;
import com.devoluciones.api.core.domain.models.pagination.SolicitudFiltro;
import com.devoluciones.api.core.usecase.solicitudes.ConsultarSolicitudesUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarSolicitudesBorradorUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesBorradorUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesFinalesUseCase;
import com.devoluciones.api.core.usecase.solicitudes.GestionarTransicionesRevisionUseCase;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.AccionSolicitudRequestDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.RechazarSolicitudRequestDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.SolicitudRequestDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.response.SolicitudResponseDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

    public SolicitudController(
            GestionarSolicitudesBorradorUseCase gestionarSolicitudesBorradorUseCase,
            GestionarTransicionesBorradorUseCase gestionarTransicionesBorradorUseCase,
            GestionarTransicionesRevisionUseCase gestionarTransicionesRevisionUseCase,
            GestionarTransicionesFinalesUseCase gestionarTransicionesFinalesUseCase,
            ConsultarSolicitudesUseCase consultarSolicitudesUseCase) {
        this.gestionarSolicitudesBorradorUseCase = gestionarSolicitudesBorradorUseCase;
        this.gestionarTransicionesBorradorUseCase = gestionarTransicionesBorradorUseCase;
        this.gestionarTransicionesRevisionUseCase = gestionarTransicionesRevisionUseCase;
        this.gestionarTransicionesFinalesUseCase = gestionarTransicionesFinalesUseCase;
        this.consultarSolicitudesUseCase = consultarSolicitudesUseCase;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(
            @Valid @RequestBody SolicitudRequestDTO request) {

        Solicitud solicitudInput = Solicitud.builder()
                .rutCliente(request.rutCliente())
                .nombreCliente(request.nombreCliente())
                .monto(request.monto())
                .bancoDestino(request.bancoDestino())
                .cuentaDestino(request.cuentaDestino())
                .referenciaBanco(request.referenciaBanco())
                .creadaPor(request.usuario().username())
                .build();

        Solicitud creada = gestionarSolicitudesBorradorUseCase.crear(solicitudInput);
        SolicitudResponseDTO responseDTO = aResponseDTO(creada);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> actualizarSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudRequestDTO request) {

        Solicitud datosModificados = Solicitud.builder()
                .rutCliente(request.rutCliente())
                .nombreCliente(request.nombreCliente())
                .monto(request.monto())
                .bancoDestino(request.bancoDestino())
                .cuentaDestino(request.cuentaDestino())
                .referenciaBanco(request.referenciaBanco())
                .build();

        Solicitud actualizada = gestionarSolicitudesBorradorUseCase.actualizar(id, datosModificados, request.usuario().username());
        SolicitudResponseDTO responseDTO = aResponseDTO(actualizada);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<SolicitudResponseDTO> enviarARevision(
            @PathVariable Long id,
            @Valid @RequestBody AccionSolicitudRequestDTO request) {

        Solicitud enviada = gestionarTransicionesBorradorUseCase.enviarARevision(
                id, request.usuario().username(), request.comentario()
        );
        return ResponseEntity.ok(aResponseDTO(enviada));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<SolicitudResponseDTO> anular(
            @PathVariable Long id,
            @Valid @RequestBody AccionSolicitudRequestDTO request) {

        Solicitud anulada = gestionarTransicionesBorradorUseCase.anular(
                id, request.usuario().username(), request.comentario()
        );
        return ResponseEntity.ok(aResponseDTO(anulada));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<SolicitudResponseDTO> aprobar(
            @PathVariable Long id,
            @Valid @RequestBody AccionSolicitudRequestDTO request) {

        Solicitud aprobada = gestionarTransicionesRevisionUseCase.aprobar(
                id, request.usuario(), request.comentario()
        );
        return ResponseEntity.ok(aResponseDTO(aprobada));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<SolicitudResponseDTO> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazarSolicitudRequestDTO request) {

        Solicitud rechazada = gestionarTransicionesRevisionUseCase.rechazar(
                id, request.usuario(), request.motivoRechazo(), request.comentario()
        );
        return ResponseEntity.ok(aResponseDTO(rechazada));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<SolicitudResponseDTO> pagar(
            @PathVariable Long id,
            @Valid @RequestBody AccionSolicitudRequestDTO request) {

        Solicitud pagada = gestionarTransicionesFinalesUseCase.pagar(
                id, request.usuario(), request.comentario()
        );
        return ResponseEntity.ok(aResponseDTO(pagada));
    }

    @PostMapping("/{id}/reabrir")
    public ResponseEntity<SolicitudResponseDTO> reabrir(
            @PathVariable Long id,
            @Valid @RequestBody AccionSolicitudRequestDTO request) {

        Solicitud reabierta = gestionarTransicionesFinalesUseCase.reabrir(
                id, request.usuario(), request.comentario()
        );
        return ResponseEntity.ok(aResponseDTO(reabierta));
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
                .map(this::aResponseDTO)
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
    public ResponseEntity<SolicitudResponseDTO> obtenerPorId(@PathVariable Long id) {
        Solicitud solicitud = consultarSolicitudesUseCase.obtenerPorId(id);
        SolicitudResponseDTO responseDTO = aResponseDTO(solicitud);
        return ResponseEntity.ok(responseDTO);
    }

    private SolicitudResponseDTO aResponseDTO(Solicitud s) {
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
}
