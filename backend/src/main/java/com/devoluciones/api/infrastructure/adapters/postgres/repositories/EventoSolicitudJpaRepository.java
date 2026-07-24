package com.devoluciones.api.infrastructure.adapters.postgres.repositories;

import com.devoluciones.api.infrastructure.adapters.postgres.entities.EventoSolicitudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoSolicitudJpaRepository extends JpaRepository<EventoSolicitudEntity, Long> {

    List<EventoSolicitudEntity> findBySolicitudIdOrderByFechaAsc(Long solicitudId);

}
