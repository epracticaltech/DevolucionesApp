-- Secuencia unificada para folios de solicitudes (DEV-AAAA-NNNNNN)
CREATE SEQUENCE IF NOT EXISTS seq_folio_solicitud START WITH 1 INCREMENT BY 1;

-- Tabla de Cargas Masivas
CREATE TABLE cargas_masivas (
    id BIGSERIAL PRIMARY KEY,
    nombre_archivo VARCHAR(255) NOT NULL,
    total_filas INT NOT NULL DEFAULT 0,
    filas_procesadas INT NOT NULL DEFAULT 0,
    filas_rechazadas INT NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL DEFAULT 'PROCESANDO',
    fecha_carga TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Detalles de Error por Fila
CREATE TABLE detalles_carga_error (
    id BIGSERIAL PRIMARY KEY,
    carga_id BIGINT NOT NULL REFERENCES cargas_masivas(id) ON DELETE CASCADE,
    numero_fila INT NOT NULL,
    campo VARCHAR(100),
    motivo TEXT NOT NULL
);

CREATE INDEX idx_detalles_carga_error_carga_id ON detalles_carga_error(carga_id);
