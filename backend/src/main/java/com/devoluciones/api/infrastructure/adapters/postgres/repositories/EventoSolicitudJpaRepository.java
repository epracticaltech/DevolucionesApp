package com.devoluciones.api.infrastructure.adapters.postgres.repositories;

import com.devoluciones.api.infrastructure.adapters.postgres.entities.EventoSolicitudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoSolicitudJpaRepository extends JpaRepository<EventoSolicitudEntity, Long> {

    List<EventoSolicitudEntity> findBySolicitudIdOrderByFechaAsc(Long solicitudId);

}
