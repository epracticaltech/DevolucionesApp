package com.devoluciones.api.infrastructure.adapters.postgres.specifications;

import com.devoluciones.api.core.domain.models.enums.EstadoSolicitud;
import com.devoluciones.api.core.domain.models.enums.OrigenSolicitud;
import com.devoluciones.api.infrastructure.adapters.postgres.entities.SolicitudEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Especificaciones dinámicas JPA para construir consultas SQL optimizadas en PostgreSQL.
 */
public class SolicitudSpecifications {

    public static Specification<SolicitudEntity> conEstado(EstadoSolicitud estado) {
        return (root, query, cb) -> (estado == null) ? cb.conjunction() : cb.equal(root.get("estado"), estado);
    }

    public static Specification<SolicitudEntity> conRut(String rutCliente) {
        return (root, query, cb) -> (rutCliente == null || rutCliente.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("rutCliente")), "%" + rutCliente.trim().toLowerCase() + "%");
    }

    public static Specification<SolicitudEntity> conOrigen(OrigenSolicitud origen) {
        return (root, query, cb) -> (origen == null) ? cb.conjunction() : cb.equal(root.get("origen"), origen);
    }

    public static Specification<SolicitudEntity> entreFechas(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        return (root, query, cb) -> {
            if (fechaDesde != null && fechaHasta != null) {
                return cb.between(root.get("fechaCreacion"), fechaDesde, fechaHasta);
            } else if (fechaDesde != null) {
                return cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaDesde);
            } else if (fechaHasta != null) {
                return cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaHasta);
            } else {
                return cb.conjunction();
            }
        };
    }
}
