package com.devoluciones.api.infrastructure.entrypoints.controllers;

import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import com.devoluciones.api.core.domain.models.pagination.PaginaResultado;
import com.devoluciones.api.core.domain.models.pagination.SolicitudFiltro;
import com.devoluciones.api.core.usecase.solicitudes.ActualizarSolicitudUseCase;
import com.devoluciones.api.core.usecase.solicitudes.CrearSolicitudUseCase;
import com.devoluciones.api.core.usecase.solicitudes.ListarSolicitudesUseCase;
import com.devoluciones.api.core.usecase.solicitudes.ObtenerSolicitudPorIdUseCase;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.ActualizarSolicitudRequestDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.CrearSolicitudRequestDTO;
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

    private final CrearSolicitudUseCase crearSolicitudUseCase;
    private final ActualizarSolicitudUseCase actualizarSolicitudUseCase;
    private final ObtenerSolicitudPorIdUseCase obtenerSolicitudPorIdUseCase;
    private final ListarSolicitudesUseCase listarSolicitudesUseCase;

    public SolicitudController(
            CrearSolicitudUseCase crearSolicitudUseCase,
            ActualizarSolicitudUseCase actualizarSolicitudUseCase,
            ObtenerSolicitudPorIdUseCase obtenerSolicitudPorIdUseCase,
            ListarSolicitudesUseCase listarSolicitudesUseCase) {
        this.crearSolicitudUseCase = crearSolicitudUseCase;
        this.actualizarSolicitudUseCase = actualizarSolicitudUseCase;
        this.obtenerSolicitudPorIdUseCase = obtenerSolicitudPorIdUseCase;
        this.listarSolicitudesUseCase = listarSolicitudesUseCase;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(
            @Valid @RequestBody CrearSolicitudRequestDTO request) {

        Solicitud solicitudInput = Solicitud.builder()
                .rutCliente(request.rutCliente())
                .nombreCliente(request.nombreCliente())
                .monto(request.monto())
                .bancoDestino(request.bancoDestino())
                .cuentaDestino(request.cuentaDestino())
                .referenciaBanco(request.referenciaBanco())
                .creadaPor(request.usuario().username())
                .build();

        Solicitud creada = crearSolicitudUseCase.ejecutar(solicitudInput);
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
            @Valid @RequestBody ActualizarSolicitudRequestDTO request) {

        Solicitud datosModificados = Solicitud.builder()
                .rutCliente(request.rutCliente())
                .nombreCliente(request.nombreCliente())
                .monto(request.monto())
                .bancoDestino(request.bancoDestino())
                .cuentaDestino(request.cuentaDestino())
                .referenciaBanco(request.referenciaBanco())
                .build();

        Solicitud actualizada = actualizarSolicitudUseCase.ejecutar(id, datosModificados, request.usuario().username());
        SolicitudResponseDTO responseDTO = aResponseDTO(actualizada);

        return ResponseEntity.ok(responseDTO);
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

        PaginaResultado<Solicitud> resultadoDominio = listarSolicitudesUseCase.ejecutar(filtro, page, size);

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
        Solicitud solicitud = obtenerSolicitudPorIdUseCase.ejecutar(id);
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
