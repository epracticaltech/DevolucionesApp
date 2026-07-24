package com.devoluciones.api.infrastructure.adapters.postgres.repositories;

import com.devoluciones.api.infrastructure.adapters.postgres.entities.SolicitudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolicitudJpaRepository extends JpaRepository<SolicitudEntity, Long> {
    Optional<SolicitudEntity> findByFolio(String folio);
    boolean existsByReferenciaBanco(String referenciaBanco);
}
