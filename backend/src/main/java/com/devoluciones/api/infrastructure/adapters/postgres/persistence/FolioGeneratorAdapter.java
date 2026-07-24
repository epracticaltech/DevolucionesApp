package com.devoluciones.api.infrastructure.adapters.postgres.persistence;

import com.devoluciones.api.core.domain.port.FolioGeneratorPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class FolioGeneratorAdapter implements FolioGeneratorPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public String generarFolio() {
        Number nextVal = (Number) entityManager
                .createNativeQuery("SELECT nextval('seq_folio_solicitud')")
                .getSingleResult();

        int anioActual = LocalDateTime.now().getYear();
        return String.format("DEV-%d-%06d", anioActual, nextVal.longValue());
    }
}
