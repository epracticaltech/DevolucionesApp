package com.devoluciones.api.infrastructure.entrypoints.controllers;

import com.devoluciones.api.core.domain.models.cargas.CargaMasiva;
import com.devoluciones.api.core.usecase.cargas.ProcesarCargaMasivaUseCase;
import com.devoluciones.api.infrastructure.entrypoints.dto.response.CargaMasivaResponseDTO;
import com.devoluciones.api.infrastructure.entrypoints.dto.response.DetalleCargaErrorDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cargas-masivas")
public class CargaMasivaController {

    private final ProcesarCargaMasivaUseCase procesarCargaMasivaUseCase;

    public CargaMasivaController(ProcesarCargaMasivaUseCase procesarCargaMasivaUseCase) {
        this.procesarCargaMasivaUseCase = procesarCargaMasivaUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CargaMasivaResponseDTO> procesarCargaMasiva(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV provisto está vacío.");
        }

        try {
            CargaMasiva cargaResultante = procesarCargaMasivaUseCase.procesarArchivoCsv(
                    file.getOriginalFilename(), file.getInputStream()
            );

            CargaMasivaResponseDTO responseDTO = aResponseDTO(cargaResultante);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(cargaResultante.getId())
                    .toUri();

            return ResponseEntity.created(location).body(responseDTO);
        } catch (Exception e) {
            throw new RuntimeException("Error procesando el archivo CSV: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargaMasivaResponseDTO> obtenerResumenCarga(@PathVariable Long id) {
        CargaMasiva carga = procesarCargaMasivaUseCase.obtenerResumenCarga(id);
        return ResponseEntity.ok(aResponseDTO(carga));
    }

    private CargaMasivaResponseDTO aResponseDTO(CargaMasiva c) {
        List<DetalleCargaErrorDTO> erroresDTO = c.getErrores().stream()
                .map(err -> new DetalleCargaErrorDTO(err.getNumeroFila(), err.getCampo(), err.getMotivo()))
                .toList();

        return new CargaMasivaResponseDTO(
                c.getId(),
                c.getNombreArchivo(),
                c.getTotalFilas(),
                c.getFilasProcesadas(),
                c.getFilasRechazadas(),
                c.getEstado(),
                c.getFechaCarga(),
                erroresDTO
        );
    }
}
