package com.devoluciones.api.infrastructure.entrypoints.controllers;

import com.devoluciones.api.core.domain.models.Solicitud;
import com.devoluciones.api.core.usecase.solicitudes.CrearSolicitudUseCase;
import com.devoluciones.api.infrastructure.entrypoints.dto.request.CrearSolicitudRequestDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.response.SolicitudResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final CrearSolicitudUseCase crearSolicitudUseCase;

    public SolicitudController(CrearSolicitudUseCase crearSolicitudUseCase) {
        this.crearSolicitudUseCase = crearSolicitudUseCase;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(
            @Valid @RequestBody CrearSolicitudRequestDTO request) {

        // 1. Mapear datos crudos del DTO de entrada al modelo de dominio Solicitud
        Solicitud solicitudInput = Solicitud.builder()
                .rutCliente(request.rutCliente())
                .nombreCliente(request.nombreCliente())
                .monto(request.monto())
                .bancoDestino(request.bancoDestino())
                .cuentaDestino(request.cuentaDestino())
                .referenciaBanco(request.referenciaBanco())
                .creadaPor(request.usuario().username())
                .build();

        // 2. Ejecutar Caso de Uso
        Solicitud creada = crearSolicitudUseCase.ejecutar(solicitudInput);

        // 3. Mapear Dominio a DTO de Respuesta
        SolicitudResponseDTO responseDTO = aResponseDTO(creada);

        // 4. Retornar 201 Created + Cabecera Location
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
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
