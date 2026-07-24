-- Secuencia unificada para folios de solicitudes (DEV-AAAA-NNNNNN)
CREATE SEQUENCE IF NOT EXISTS seq_folio_solicitud START WITH 1 INCREMENT BY 1;

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    mail VARCHAR(100) NOT NULL UNIQUE,
    rol VARCHAR(30) NOT NULL
);

CREATE TABLE solicitudes (
    id BIGSERIAL PRIMARY KEY,
    folio VARCHAR(20) NOT NULL UNIQUE,
    rut_cliente VARCHAR(12) NOT NULL,
    nombre_cliente VARCHAR(100) NOT NULL,
    monto NUMERIC(12, 2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'CLP',
    banco_destino VARCHAR(50) NOT NULL,
    cuenta_destino VARCHAR(30) NOT NULL,
    referencia_banco VARCHAR(50),
    origen VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    motivo_rechazo VARCHAR(255),
    veces_reabierta INT NOT NULL DEFAULT 0,
    creada_por VARCHAR(50) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizada_por VARCHAR(50),
    fecha_actualizacion TIMESTAMP
);

CREATE TABLE eventos_solicitud (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes(id) ON DELETE CASCADE,
    estado_origen VARCHAR(20) NOT NULL,
    estado_destino VARCHAR(20) NOT NULL,
    usuario VARCHAR(50) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comentario VARCHAR(255)
);

CREATE INDEX idx_solicitudes_estado ON solicitudes(estado);
CREATE INDEX idx_solicitudes_rut ON solicitudes(rut_cliente);
CREATE INDEX idx_solicitudes_ref_banco ON solicitudes(referencia_banco);
