package com.devoluciones.api.infrastructure.adapters.postgres.repositories;

import com.devoluciones.api.infrastructure.adapters.postgres.entities.CargaMasivaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CargaMasivaJpaRepository extends JpaRepository<CargaMasivaEntity, Long> {
}
