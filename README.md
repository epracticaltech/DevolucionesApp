# DevolucionesApp — Sistema de Gestión y Devolución de Fondos Fullstack

Sistema fullstack para la digitalización y automatización del proceso de devolución de pagos duplicados o en exceso en la administradora de fondos.

---

## 1. Guía Rápida de Despliegue (Cómo levantar el proyecto)

### Opción A: Despliegue Completo con Docker Compose (Recomendado para Producción)

Ejecuta el siguiente comando en la raíz del proyecto para construir y levantar la base de datos PostgreSQL, el backend Spring Boot y el frontend Angular:

```bash
docker compose up --build
```

- **Frontend Angular:** [http://localhost:4200](http://localhost:4200)
- **Backend API REST:** [http://localhost:8080](http://localhost:8080)
- **Credenciales Seed de Prueba:**
  - **Analista:** `analista1` / `password123` (Rol: `ANALISTA`)
  - **Supervisor:** `supervisor1` / `password123` (Rol: `SUPERVISOR`)

---

### Opción B: Ejecución en Entorno de Desarrollo Local

Si prefieres ejecutar el código fuente localmente para depuración:

1. **Levantar la Base de Datos PostgreSQL en Docker:**
   ```bash
   docker compose down -v
   docker compose up -d db
   ```

2. **Iniciar el Backend Spring Boot (Java 21):**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. **Iniciar el Frontend Angular 17:**
   ```bash
   cd frontend
   npm start
   ```

---

## 2. Arquitectura del Proyecto (Clean Architecture + Angular 17 Standalone)

El proyecto fue construido siguiendo los principios de **Clean Architecture (Arquitectura Limpia)** en el backend y **Angular 17 Standalone Components** en el frontend:

```text
nxtara/DevolucionesApp
├── backend/                       # API REST Spring Boot 3.4 / Java 21
│   ├── core/                      # Capa de Dominio y Casos de Uso (Núcleo de Negocio)
│   │   ├── domain/
│   │   │   ├── exceptions/        # Excepciones semánticas (RecursoNoEncontrado, TransicionInvalida, ReglaNegocio, etc.)
│   │   │   ├── models/            # Entidades puras del dominio (Solicitud, Usuario, EventoSolicitud, CargaMasiva)
│   │   │   └── port/              # Puertos de salida (SolicitudRepositoryPort, UsuarioRepositoryPort, FolioGeneratorPort)
│   │   └── usecase/
│   │       ├── auth/              # Caso de uso de autenticación JWT (AutenticarUsuarioUseCase)
│   │       ├── cargas/            # Procesamiento batch de CSV (ProcesarCargaMasivaUseCase)
│   │       └── solicitudes/       # Casos de uso consolidados por ciclo de vida
│   └── infrastructure/            # Adaptadores de infraestructura
│       ├── adapters/postgres/     # Adaptadores JPA PostgreSQL (SolicitudEntity, UsuarioEntity, CargaMasivaEntity)
│       ├── security/              # Spring Security stateless JWT, JwtTokenProvider, CustomAuthEntryPoint (401), CustomAccessDenied (403)
│       └── entrypoints/
│           ├── controllers/       # Endpoints REST (SolicitudController, AuthController, CargaMasivaController)
│           └── dto/               # Records Java de transferencia de datos
└── frontend/                      # Aplicación Angular 17 Standalone
    ├── src/app/
    │   ├── core/
    │   │   ├── guards/            # authGuard (CanActivateFn)
    │   │   ├── interceptors/      # jwtInterceptor (HttpInterceptorFn para Bearer token)
    │   │   ├── models/            # Interfaces TypeScript tipadas (SolicitudResponse, AuthResponse, etc.)
    │   │   └── services/          # AuthService (Signals + localStorage) y SolicitudService (HttpClient)
    │   ├── features/
    │   │   ├── auth/              # LoginComponent (/login)
    │   │   ├── solicitudes/       # BandejaComponent, DetalleComponent, FormularioComponent
    │   │   └── carga-masiva/      # CargaMasivaComponent (Lazy Loaded)
    │   └── shared/layout/         # LayoutComponent con Navbar y User Badge
    └── src/environments/          # environment.ts (apiUrl: 'http://localhost:8080/api/v1')
```

---

## 3. Resumen de Implementación de la Prueba Técnica

### Parte 1 — API REST con Máquina de Estados (100% Implementada)
- **Máquina de Estados Cohesiva:** Transiciones declarativas e inmutables mediante acciones HTTP dedicadas (`POST /enviar`, `/aprobar`, `/rechazar`, `/pagar`, `/reabrir`, `/anular`).
- **Reglas de Negocio Enforzadas (R1–R7):**
  - **R1:** Respuestas HTTP 409 Conflict ante transiciones inválidas.
  - **R2:** Control estricto de roles (`SUPERVISOR` para aprobar/rechazar/pagar).
  - **R3:** Exigencia de `motivo_rechazo` en estado `RECHAZADA`.
  - **R4:** Reapertura de solicitudes rechazadas permitida máximo 1 vez (`veces_reabierta`).
  - **R5:** Validación de RUT chileno (Módulo 11) y montos entre 1 y 10.000.000 CLP.
  - **R6:** Registro inmutable de eventos de auditoría (`EventoSolicitud`) en la misma transacción (`@Transactional`).
  - **R7:** Separación de funciones: el supervisor que aprueba no puede ser el creador de la solicitud.
- **Paginación y Filtros en BD:** Consultas optimizadas con JPA Specification y `Pageable` para evitar cargar colecciones en memoria.

### Parte 2 — Carga Masiva CSV (100% Implementada)
- **Procesamiento Batch:** Inserción en bloques (chunks) de 100 filas con `flush` y `clear` del EntityManager.
- **Tolerancia a Filas Defectuosas:** Las filas con errores no detienen el procesamiento; se registran en `detalles_carga_error` (`numero_fila`, `campo`, `motivo`). Con el archivo `pagos_banco_ejemplo.csv`, ~950 filas entran en `EN_REVISION` y ~50 se reportan detalladamente.
- **Idempotencia:** Evita la duplicación verificando la clave única `referencia_banco`.

### Parte 3 — Frontend Angular 17 (100% Implementado)
- **Componentes Standalone:** Desarrollo moderno sin `NgModules`.
- **Autenticación & Interceptores:** Guard de rutas (`authGuard`) e interceptor HTTP (`jwtInterceptor`) que adjunta automáticamente el token `Bearer JWT`.
- **Bandeja, Detalle y Formulario:**
  - Tabla paginada con badges de color por estado.
  - Botones de acción dinámicos filtrados por estado de la solicitud y rol del usuario logueado.
  - Formulario reactivo (`ReactiveFormsModule`) con validaciones espejo del backend.
- **Carga Masiva:** Interfaz de subida con resumen y tabla interactiva de errores por fila.

### Parte 4 — Seguridad JWT + RBAC (100% Implementada)
- Spring Security Stateless con codificación de contraseñas mediante **BCrypt**.
- Excepciones personalizadas HTTP 401 (No Autorizado) y HTTP 403 (Prohibido por Falta de Rol).
- Pruebas unitarias de seguridad (`RbacSecurityTest`) que garantizan el control de acceso.

---

## 4. Parte 5 — Reporte de Conciliación e Índices PostgreSQL (Explicación Teórica)

Debido al tiempo del desarrollo, la **Parte 5** se detalla teóricamente a continuación:

### A. Endpoint Diseñado
```text
GET /api/v1/reportes/conciliacion?desde=2026-07-01&hasta=2026-07-21
```

### B. Agregación a Nivel de Base de Datos (`GROUP BY`)
La consolidación de montos y totales diarios debe ser realizada directamente por PostgreSQL para garantizar máximo rendimiento:

```sql
SELECT 
    CAST(fecha_creacion AS DATE) AS fecha,
    estado,
    COUNT(id) AS total_solicitudes,
    SUM(monto) AS monto_total
FROM solicitudes
WHERE fecha_creacion >= :fechaDesde 
  AND fecha_creacion <= :fechaHasta
GROUP BY CAST(fecha_creacion AS DATE), estado
ORDER BY fecha ASC, estado ASC;
```

### C. Estrategia de Índices para 5 Millones de Registros
Para soportar millones de filas sin degradación de I/O en disco:

```sql
-- Índice compuesto para filtrado por fecha y agrupación por estado
CREATE INDEX idx_solicitudes_fecha_estado 
ON solicitudes (fecha_creacion, estado) 
INCLUDE (monto);
```

- **Justificación de `INCLUDE (monto)`:** Permite realizar un **Index Only Scan**, satisfaciendo la consulta completamente desde el árbol B-Tree del índice sin necesidad de leer las páginas de datos en el Heap del disco.
- **Análisis de Rendimiento (`EXPLAIN ANALYZE`):**
  - **Sin índice:** Realiza un `Seq Scan` (escaneo secuencial) de complejidad $O(N)$ con alto tiempo de ejecución.
  - **Con `idx_solicitudes_fecha_estado`:** Ejecuta un `Index Only Scan` de complejidad $O(\log N)$, reduciendo la respuesta a pocos milisegundos.

---

## 5. Pruebas Automatizadas (Backend & Frontend)

### Ejecución de Pruebas Backend
```bash
cd backend
./mvnw test
```

- **Resultado:** `40/40 tests pasados exitosamente (BUILD SUCCESS)`.
- Cubre la máquina de estados, reglas R1–R7, controladores REST, validaciones y RBAC.

### Compilación Frontend
```bash
cd frontend
npm run build
```
- Compilación limpia generando artefactos optimizados con Lazy Loading en `dist/frontend`.

---

## 6. Reflexión Honesta y Agradecimientos

Durante esta prueba técnica se priorizó la construcción de un **núcleo sólido, robusto y testeado (Partes 1, 2, 3 y 4)** con Clean Architecture, Spring Security stateless y Angular 17 Standalone.

Por limitaciones de tiempo, la Parte 5 (Reporte de Conciliación) se incluyó de forma teórica en esta documentación. El proceso representó un excelente desafío técnico que requirió investigar e integrar patrones avanzados de arquitectura y seguridad.

**¡Muchas gracias por la oportunidad de participar en esta prueba técnica!**
