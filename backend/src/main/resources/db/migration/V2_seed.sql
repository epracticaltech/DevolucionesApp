-- Seed de Usuarios con Passwords BCrypt ('password123')
INSERT INTO usuarios (username, password, mail, rol) VALUES
('analista1', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xd00DMxs.AQubh4a', 'analista1@devoluciones.cl', 'ANALISTA'),
('analista2', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xd00DMxs.AQubh4a', 'analista2@devoluciones.cl', 'ANALISTA'),
('supervisor1', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xd00DMxs.AQubh4a', 'supervisor1@devoluciones.cl', 'SUPERVISOR'),
('supervisor2', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xd00DMxs.AQubh4a', 'supervisor2@devoluciones.cl', 'SUPERVISOR');

-- Seed de Solicitudes repartidas en los 6 estados con referencias bancarias e historiales coherentes
INSERT INTO solicitudes (folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino, referencia_banco, origen, estado, motivo_rechazo, veces_reabierta, creada_por, actualizada_por) VALUES
('DEV-2026-000001', '12345678-5', 'MARIA PEREZ SOTO', 150000.00, 'CLP', 'BANCO CHILE', '00123456', 'REF-SEED-001', 'MANUAL', 'BORRADOR', NULL, 0, 'analista1', 'analista1'),
('DEV-2026-000002', '11111111-1', 'CARLOS GONZALEZ', 450000.00, 'CLP', 'BCI', '99887766', 'REF-SEED-002', 'MANUAL', 'BORRADOR', 'Inconsistencia en comprobante de transferencia', 1, 'analista2', 'analista2'),
('DEV-2026-000003', '9685216-9', 'FELIPE FLORES ROSAS', 50000.00, 'CLP', 'SANTANDER', '77665544', 'REF-SEED-003', 'MANUAL', 'EN_REVISION', NULL, 0, 'analista1', 'analista1'),
('DEV-2026-000004', '8765432-1', 'ANDREA MORALES', 120000.00, 'CLP', 'SCOTIABANK', '55443322', 'REF-SEED-004', 'CARGA_MASIVA', 'EN_REVISION', NULL, 0, 'SISTEMA_CARGA_MASIVA', 'SISTEMA_CARGA_MASIVA'),
('DEV-2026-000005', '15555555-4', 'JUAN PABLO SILVA', 300000.00, 'CLP', 'BANCO CHILE', '11223344', 'REF-SEED-005', 'MANUAL', 'APROBADA', NULL, 0, 'analista1', 'supervisor1'),
('DEV-2026-000006', '16666666-2', 'RODRIGO VAZQUEZ', 75000.00, 'CLP', 'ITAÚ', '66778899', 'REF-SEED-006', 'MANUAL', 'APROBADA', NULL, 0, 'analista2', 'supervisor2'),
('DEV-2026-000007', '17777777-0', 'CAMILA FUENTES', 200000.00, 'CLP', 'BCI', '33445566', 'REF-SEED-007', 'MANUAL', 'RECHAZADA', 'Monto no corresponde a pago duplicado registrado', 0, 'analista1', 'supervisor1'),
('DEV-2026-000008', '18888888-9', 'DANIEL ROJAS BASTIAS', 500000.00, 'CLP', 'BANCO CHILE', '99001122', 'REF-SEED-008', 'MANUAL', 'PAGADA', NULL, 0, 'analista1', 'supervisor1'),
('DEV-2026-000009', '19999999-7', 'LORETO BRAVO SANCHEZ', 850000.00, 'CLP', 'SANTANDER', '44556677', 'REF-SEED-009', 'MANUAL', 'PAGADA', NULL, 0, 'analista2', 'supervisor2'),
('DEV-2026-000010', '20111222-3', 'ESTEBAN CASTRO', 90000.00, 'CLP', 'BANCO ESTADO', '123123123', 'REF-SEED-010', 'MANUAL', 'ANULADA', NULL, 0, 'analista1', 'analista1');

-- Historial Coherente de Eventos de Auditoría (Punto 2.4)
INSERT INTO eventos_solicitud (solicitud_id, estado_origen, estado_destino, usuario, comentario) VALUES
-- Solicitud 1: BORRADOR
(1, NULL, 'BORRADOR', 'analista1', 'Creación manual de solicitud de devolución'),

-- Solicitud 2: BORRADOR (reabierta por analista2)
(2, NULL, 'BORRADOR', 'analista2', 'Creación manual de solicitud de devolución'),
(2, 'BORRADOR', 'EN_REVISION', 'analista2', 'Enviado a revisión del supervisor'),
(2, 'EN_REVISION', 'RECHAZADA', 'supervisor1', 'Rechazado: Inconsistencia en comprobante de transferencia'),
(2, 'RECHAZADA', 'BORRADOR', 'analista2', 'Solicitud reabierta para corrección de antecedentes (Intento 1/1)'),

-- Solicitud 3: EN_REVISION
(3, NULL, 'BORRADOR', 'analista1', 'Creación manual de solicitud de devolución'),
(3, 'BORRADOR', 'EN_REVISION', 'analista1', 'Enviado a revisión del supervisor'),

-- Solicitud 4: EN_REVISION (Masiva)
(4, 'EN_REVISION', 'EN_REVISION', 'SISTEMA_CARGA_MASIVA', 'Creación automática desde Carga Masiva (Archivo: pagos_banco_ejemplo.csv)'),

-- Solicitud 5: APROBADA
(5, NULL, 'BORRADOR', 'analista1', 'Creación manual de solicitud de devolución'),
(5, 'BORRADOR', 'EN_REVISION', 'analista1', 'Enviado a revisión del supervisor'),
(5, 'EN_REVISION', 'APROBADA', 'supervisor1', 'Solicitud aprobada por el supervisor'),

-- Solicitud 6: APROBADA (por supervisor2)
(6, NULL, 'BORRADOR', 'analista2', 'Creación manual de solicitud de devolución'),
(6, 'BORRADOR', 'EN_REVISION', 'analista2', 'Enviado a revisión del supervisor'),
(6, 'EN_REVISION', 'APROBADA', 'supervisor2', 'Solicitud aprobada por el supervisor'),

-- Solicitud 7: RECHAZADA
(7, NULL, 'BORRADOR', 'analista1', 'Creación manual de solicitud de devolución'),
(7, 'BORRADOR', 'EN_REVISION', 'analista1', 'Enviado a revisión del supervisor'),
(7, 'EN_REVISION', 'RECHAZADA', 'supervisor1', 'Rechazado: Monto no corresponde a pago duplicado registrado'),

-- Solicitud 8: PAGADA
(8, NULL, 'BORRADOR', 'analista1', 'Creación manual de solicitud de devolución'),
(8, 'BORRADOR', 'EN_REVISION', 'analista1', 'Enviado a revisión del supervisor'),
(8, 'EN_REVISION', 'APROBADA', 'supervisor1', 'Solicitud aprobada por el supervisor'),
(8, 'APROBADA', 'PAGADA', 'supervisor1', 'Pago de la devolución efectuado exitosamente'),

-- Solicitud 9: PAGADA (por supervisor2)
(9, NULL, 'BORRADOR', 'analista2', 'Creación manual de solicitud de devolución'),
(9, 'BORRADOR', 'EN_REVISION', 'analista2', 'Enviado a revisión del supervisor'),
(9, 'EN_REVISION', 'APROBADA', 'supervisor2', 'Solicitud aprobada por el supervisor'),
(9, 'APROBADA', 'PAGADA', 'supervisor2', 'Pago de la devolución efectuado exitosamente'),

-- Solicitud 10: ANULADA
(10, NULL, 'BORRADOR', 'analista1', 'Creación manual de solicitud de devolución'),
(10, 'BORRADOR', 'ANULADA', 'analista1', 'Solicitud anulada por el analista creador');
