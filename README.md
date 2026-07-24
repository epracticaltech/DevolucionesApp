# DevolucionesApp — MVP Backend (Parte 1 Finalizada)

Sistema de gestión y automatización para la devolución de pagos duplicados o en exceso en la administradora de fondos.

---

## 1. Arquitectura del Proyecto (Clean Architecture Personalizada)

El proyecto se diseñó utilizando los principios de **Clean Architecture (Arquitectura Limpia)**, estructurando las capas de manera desacoplada para mantener las reglas de negocio aisladas de cualquier marco de trabajo o detalle de infraestructura:

```text
com.devoluciones.api
├── core/                         # Capa de Dominio y Casos de Uso (Núcleo de Negocio)
│   ├── domain/
│   │   ├── exceptions/           # Excepciones semánticas (RecursoNoEncontrado, TransicionInvalida, etc.)
│   │   ├── models/               # Entidades y objetos de valor del dominio (Solicitud, EventoSolicitud)
│   │   │   ├── enums/            # Máquina de Estados cohesiva (EstadoSolicitud, OrigenSolicitud)
│   │   │   └── pagination/       # Abstracciones de filtrado y paginación desacopladas (PaginaResultado)
│   │   └── port/                 # Puertos de salida del dominio (SolicitudRepositoryPort)
│   └── usecase/
│       └── solicitudes/          # Casos de Uso consolidados por ciclo de vida
│           ├── GestionarSolicitudesBorradorUseCase.java
│           ├── GestionarTransicionesBorradorUseCase.java
│           ├── GestionarTransicionesRevisionUseCase.java
│           ├── GestionarTransicionesFinalesUseCase.java
│           └── ConsultarSolicitudesUseCase.java
├── infrastructure/               # Capa de Infraestructura (Detalles de implementación)
│   ├── adapters/
│   │   └── postgres/             # Adaptador de persistencia PostgreSQL / JPA
│   │       ├── entities/         # Entidades JPA (SolicitudEntity, EventoSolicitudEntity)
│   │       ├── persistence/      # Implementación del puerto SolicitudRepositoryAdapter
│   │       ├── repositories/     # Interfaces Spring Data JPA
│   │       └── specifications/   # Consultas dinámicas JPA Specifications
│   ├── config/                   # Configuración de Spring Beans y Transacciones (@Transactional)
│   └── entrypoints/
│       ├── controllers/          # Controladores REST API y Manejo Global (@RestControllerAdvice)
│       ├── dto/                  # DTOs de Entrada/Salida encapsulados como Java Records
│       └── validation/           # Validadores personalizados (Módulo 11 RUT)
└── shared/                       # Utilidades transversales puras (RutUtils)
```

---

## 2. Contrato de la API REST (Parte 1 Completada)

| Método | Endpoint | Status Válido | Status de Error | Descripción |
|---|---|---|---|---|
| `POST` | `/api/v1/solicitudes` | **201 Created** | 400 | Crear solicitud manual en estado `BORRADOR` con asignación de folio único (`DEV-AAAA-NNNNNN`). |
| `GET` | `/api/v1/solicitudes` | **200 OK** | — | Consulta paginada (`page`, `size`) con filtros combinables en BD (`estado`, `rut`, `origen`, `fechaDesde`, `fechaHasta`). |
| `GET` | `/api/v1/solicitudes/{id}` | **200 OK** | 404 | Buscar solicitud por ID. |
| `PUT` | `/api/v1/solicitudes/{id}` | **200 OK** | 400, 409 | Editar solicitud en estado `BORRADOR`. Si el estado actual no es `BORRADOR` $\rightarrow$ **409 Conflict**. |
| `POST` | `/api/v1/solicitudes/{id}/enviar` | **200 OK** | 400, 409 | Transicionar de `BORRADOR` $\rightarrow$ `EN_REVISION` con auditoría. Requiere `referenciaBanco`. |
| `POST` | `/api/v1/solicitudes/{id}/anular` | **200 OK** | 409 | Transicionar de `BORRADOR` $\rightarrow$ `ANULADA` con auditoría. |
| `POST` | `/api/v1/solicitudes/{id}/aprobar` | **200 OK** | 400, 403, 409 | Transicionar de `EN_REVISION` $\rightarrow$ `APROBADA`. Exige rol `SUPERVISOR` (R2) y separación de funciones creador/aprobador (R7). |
| `POST` | `/api/v1/solicitudes/{id}/rechazar` | **200 OK** | 400, 403, 409 | Transicionar de `EN_REVISION` $\rightarrow$ `RECHAZADA`. Exige rol `SUPERVISOR` (R2) y `motivo_rechazo` obligatorio (R3). |
| `POST` | `/api/v1/solicitudes/{id}/pagar` | **200 OK** | 403, 409 | Transicionar de `APROBADA` $\rightarrow$ `PAGADA`. Exige rol `SUPERVISOR` (R2). |
| `POST` | `/api/v1/solicitudes/{id}/reabrir` | **200 OK** | 409 | Transicionar de `RECHAZADA` $\rightarrow$ `BORRADOR`. Permitido máx. 1 vez (R4). Permite rol `ANALISTA` y `SUPERVISOR`. |
| `GET` | `/api/v1/solicitudes/{id}/historial` | **200 OK** | 404 | Consultar el histórico inmutable de eventos de auditoría (`EventoSolicitud`) ordenado por fecha. |

---

## 3. Justificación de Decisiones de Diseño y Exigencias Técnicas

### ¿Por qué las transiciones son acciones HTTP `POST /{id}/accion` y no un `PUT` del campo `estado`?
Un `PUT` en REST representa la sustitución completa del estado o representación de un recurso. Sin embargo, en un dominio guiado por una **Máquina de Estados de Negocio**, una transición de estado no es un simple cambio de variable. Cada transición representa un **comando explicito con reglas y efectos secundarios inseparables**:
- Verificación del estado de origen permitido (Regla R1).
- Autorización y restricción de roles autorizadores (Regla R2).
- Validaciones específicas de la acción, como el motivo obligatorio en rechazo (R3) o la separación de funciones (R7).
- La inserción **obligatoria, inmutable y atómica** de un evento en el histórico `EventoSolicitud` dentro de la misma transacción (Regla R6).

Modelar cada transición como una acción explícita (`POST /{id}/aprobar`, `POST /{id}/rechazar`, etc.) comunica con claridad la **intención de negocio**, evita mutaciones de estado inconsistentes desde el cliente y garantiza la integridad de la auditoría.

### Máquina de Estados Cohesiva (`EstadoSolicitud.java`)
Toda la lógica de transición y sus restricciones vive encapsulada en el Enum `EstadoSolicitud.java` mediante el método `validarTransicionHacia(nuevoEstado)`. Si el día de mañana se agrega un nuevo estado en el ciclo de vida, solo se requiere modificar esta pieza cohesiva sin esparcir bloques `if/else` por los servicios.

### Aislamiento de Capas: DTOs vs Entidades JPA
Las entidades de persistencia `SolicitudEntity` y `EventoSolicitudEntity` pertenecen estrictamente a la capa de infraestructura. La API REST consume y retorna objetos inmutables mediante Java `records` (`SolicitudRequestDTO`, `SolicitudResponseDTO`, `AccionSolicitudRequestDTO`, `RechazarSolicitudRequestDTO`, `EventoSolicitudResponseDTO`, `ErrorResponseDTO`), garantizando que los detalles de la BD no se expongan hacia los clientes externos.

### Manejo Global de Errores (`@ControllerAdvice`)
El componente `ExceptionHandlerController` captura todas las excepciones de dominio y validación, retornando un contrato único de error:
```json
{
  "timestamp": "2026-07-24T03:30:00",
  "status": 409,
  "error": "Transición Inválida de Estado",
  "detalle": "La solicitud en estado BORRADOR no puede transicionar a PAGADA.",
  "path": "/api/v1/solicitudes/1/pagar"
}
```
Ningún error no controlado o stacktrace de Java se expone hacia el cliente final.

### Paginación Real y Filtros Dinámicos en Base de Datos
Las búsquedas combinables en `GET /api/v1/solicitudes` hacen uso de Spring Data JPA `Specifications` (`SolicitudSpecifications`), aplicando las cláusulas `WHERE` directamente en el motor **PostgreSQL** junto con `Pageable`. Los datos no se cargan en memoria para filtrar.

---

## 4. Instrucciones para Ejecutar las Pruebas Unitarias

Toda la suite de pruebas unitarias y de integración de la Parte 1 puede ejecutarse desde la carpeta `backend` con los siguientes comandos:

```bash
cd backend
./mvnw test
```

### Resultados de la Suite de Pruebas:
- **`33/33 tests pasados (BUILD SUCCESS)`** comprobando:
  - Reglas de la Máquina de Estados (`EstadoSolicitudTest`).
  - Creación manual, idempotencia y edición en borrador (`GestionarSolicitudesBorradorUseCaseTest`).
  - Transiciones de envío y anulación desde borrador (`GestionarTransicionesBorradorUseCaseTest`).
  - Transiciones de aprobación, rechazo y separación de funciones R7 (`GestionarTransicionesRevisionUseCaseTest`).
  - Transiciones de pago y límite de reapertura R4 (`GestionarTransicionesFinalesUseCaseTest`).
  - Búsqueda por ID, paginación con filtros e historial de auditoría (`ConsultarSolicitudesUseCaseTest`).
  - Verificación del contexto global de Spring Boot (`ApiApplicationTests`).

