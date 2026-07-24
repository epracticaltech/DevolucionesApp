package com.devoluciones.api.core.domain.port;

public interface FolioGeneratorPort {

    /**
     * Genera un folio único correlativo con formato DEV-AAAA-NNNNNN basado en secuencia PostgreSQL.
     */
    String generarFolio();
}
