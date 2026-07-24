package com.devoluciones.api.infrastructure.adapters.postgres.repositories;

import com.devoluciones.api.infrastructure.adapters.postgres.entities.DetalleCargaErrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCargaErrorJpaRepository extends JpaRepository<DetalleCargaErrorEntity, Long> {

    List<DetalleCargaErrorEntity> findByCargaIdOrderByNumeroFilaAsc(Long cargaId);
}
