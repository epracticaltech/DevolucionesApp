-- Seed de Usuarios (Punto 2.4 de la prueba técnica)
INSERT INTO usuarios (username, password, mail, rol) VALUES
('analista1', '$2a$10$e8w.xL2xQ6G9gH9Vp3vLGe9fKx8sZ5W8Q4k9V2jX1g0L9m8N7O6P5', 'analista1@devoluciones.cl', 'ANALISTA'),
('supervisor1', '$2a$10$e8w.xL2xQ6G9gH9Vp3vLGe9fKx8sZ5W8Q4k9V2jX1g0L9m8N7O6P5', 'supervisor1@devoluciones.cl', 'SUPERVISOR');

-- Solicitudes de Prueba repartidas en los 6 estados
INSERT INTO solicitudes (folio, rut_cliente, nombre_cliente, monto, banco_destino, cuenta_destino, origen, estado, creada_por)
VALUES 
('DEV-2026-000001', '12345678-5', 'MARIA PEREZ', 150000.00, 'BANCO CHILE', '00123456', 'MANUAL', 'BORRADOR', 'analista1'),
('DEV-2026-000002', '9685216-9', 'FELIPE FLORES', 50000.00, 'BCI', '99887766', 'MANUAL', 'EN_REVISION', 'analista1');

INSERT INTO eventos_solicitud (solicitud_id, estado_origen, estado_destino, usuario, comentario)
VALUES 
(1, 'BORRADOR', 'BORRADOR', 'analista1', 'Creación inicial'),
(2, 'BORRADOR', 'EN_REVISION', 'analista1', 'Enviado a revisión');
