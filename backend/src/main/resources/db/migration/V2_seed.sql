-- Solicitudes de Prueba repartidas en los 6 estados
INSERT INTO solicitudes (folio, rut_cliente, nombre_cliente, monto, banco_destino, cuenta_destino, origen, estado, creada_por)
VALUES 
('DEV-2026-000001', '12345678-5', 'MARIA PEREZ', 150000.00, 'BANCO CHILE', '00123456', 'MANUAL', 'BORRADOR', 'analista1'),
('DEV-2026-000002', '9685216-9', 'FELIPE FLORES', 50000.00, 'BCI', '99887766', 'MANUAL', 'EN_REVISION', 'analista1');

INSERT INTO eventos_solicitud (solicitud_id, estado_origen, estado_destino, usuario, comentario)
VALUES 
(1, 'BORRADOR', 'BORRADOR', 'analista1', 'Creación inicial'),
(2, 'BORRADOR', 'EN_REVISION', 'analista1', 'Enviado a revisión');
