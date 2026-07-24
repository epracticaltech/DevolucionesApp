package com.devoluciones.api.infrastructure.adapters.postgres.persistence;

import com.devoluciones.api.core.domain.models.cargas.CargaMasiva;
import com.devoluciones.api.core.domain.models.cargas.DetalleCargaError;
import com.devoluciones.api.core.domain.port.CargaMasivaRepositoryPort;
import com.devoluciones.api.infrastructure.adapters.postgres.entities.CargaMasivaEntity;
import com.devoluciones.api.infrastructure.adapters.postgres.entities.DetalleCargaErrorEntity;
import com.devoluciones.api.infrastructure.adapters.postgres.repositories.CargaMasivaJpaRepository;
import com.devoluciones.api.infrastructure.adapters.postgres.repositories.DetalleCargaErrorJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class CargaMasivaRepositoryAdapter implements CargaMasivaRepositoryPort {

    private final CargaMasivaJpaRepository cargaMasivaJpaRepository;
    private final DetalleCargaErrorJpaRepository detalleCargaErrorJpaRepository;

    public CargaMasivaRepositoryAdapter(
            CargaMasivaJpaRepository cargaMasivaJpaRepository,
            DetalleCargaErrorJpaRepository detalleCargaErrorJpaRepository) {
        this.cargaMasivaJpaRepository = cargaMasivaJpaRepository;
        this.detalleCargaErrorJpaRepository = detalleCargaErrorJpaRepository;
    }

    @Override
    public CargaMasiva guardar(CargaMasiva cargaMasiva) {
        CargaMasivaEntity entity = toEntity(cargaMasiva);
        CargaMasivaEntity guardada = cargaMasivaJpaRepository.save(entity);
        return toDomain(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CargaMasiva> buscarPorId(Long id) {
        return cargaMasivaJpaRepository.findById(id).map(entity -> {
            CargaMasiva domain = toDomain(entity);
            List<DetalleCargaErrorEntity> errorEntities = detalleCargaErrorJpaRepository.findByCargaIdOrderByNumeroFilaAsc(id);
            List<DetalleCargaError> errores = errorEntities.stream().map(this::toDomain).toList();
            domain.setErrores(errores);
            return domain;
        });
    }

    @Override
    public void guardarErrores(List<DetalleCargaError> errores) {
        if (errores == null || errores.isEmpty()) {
            return;
        }

        List<DetalleCargaErrorEntity> entities = errores.stream()
                .map(e -> DetalleCargaErrorEntity.builder()
                        .id(e.getId())
                        .cargaId(e.getCargaId())
                        .numeroFila(e.getNumeroFila())
                        .campo(e.getCampo())
                        .motivo(e.getMotivo())
                        .build())
                .toList();

        detalleCargaErrorJpaRepository.saveAll(entities);
    }

    private CargaMasivaEntity toEntity(CargaMasiva c) {
        return CargaMasivaEntity.builder()
                .id(c.getId())
                .nombreArchivo(c.getNombreArchivo())
                .totalFilas(c.getTotalFilas())
                .filasProcesadas(c.getFilasProcesadas())
                .filasRechazadas(c.getFilasRechazadas())
                .estado(c.getEstado() != null ? c.getEstado() : "PROCESANDO")
                .fechaCarga(c.getFechaCarga())
                .build();
    }

    private CargaMasiva toDomain(CargaMasivaEntity e) {
        return CargaMasiva.builder()
                .id(e.getId())
                .nombreArchivo(e.getNombreArchivo())
                .totalFilas(e.getTotalFilas())
                .filasProcesadas(e.getFilasProcesadas())
                .filasRechazadas(e.getFilasRechazadas())
                .estado(e.getEstado())
                .fechaCarga(e.getFechaCarga())
                .build();
    }

    private DetalleCargaError toDomain(DetalleCargaErrorEntity e) {
        return DetalleCargaError.builder()
                .id(e.getId())
                .cargaId(e.getCargaId())
                .numeroFila(e.getNumeroFila())
                .campo(e.getCampo())
                .motivo(e.getMotivo())
                .build();
    }
}
