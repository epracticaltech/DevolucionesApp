# DevolucionesApp — MVP Backend (Parte 1 y Parte 2 Finalizadas)

Sistema de gestión y automatización para la devolución de pagos duplicados o en exceso en la administradora de fondos.

---

## 1. Arquitectura del Proyecto (Clean Architecture Personalizada)

El proyecto se diseñó utilizando los principios de **Clean Architecture (Arquitectura Limpia)**, estructurando las capas de manera desacoplada para mantener las reglas de negocio aisladas de cualquier marco de trabajo o detalle de infraestructura:

```text
com.devoluciones.api
├── core/                         # Capa de Dominio y Casos de Uso (Núcleo de Negocio)
│   ├── domain/
│   │   ├── exceptions/           # Excepciones semánticas (RecursoNoEncontrado, TransicionInvalida, etc.)
│   │   ├── models/               # Entidades y objetos de valor del dominio (Solicitud, EventoSolicitud, CargaMasiva, DetalleCargaError)
│   │   │   ├── enums/            # Máquina de Estados cohesiva (EstadoSolicitud, OrigenSolicitud)
│   │   │   └── pagination/       # Abstracciones de filtrado y paginación desacopladas (PaginaResultado)
│   │   └── port/                 # Puertos de salida del dominio (SolicitudRepositoryPort, CargaMasivaRepositoryPort, FolioGeneratorPort)
│   └── usecase/
│       ├── cargas/               # Caso de Uso de Carga Masiva CSV (ProcesarCargaMasivaUseCase)
│       └── solicitudes/          # Casos de Uso consolidados por ciclo de vida
│           ├── GestionarSolicitudesBorradorUseCase.java
│           ├── GestionarTransicionesBorradorUseCase.java
│           ├── GestionarTransicionesRevisionUseCase.java
│           ├── GestionarTransicionesFinalesUseCase.java
│           └── ConsultarSolicitudesUseCase.java
├── infrastructure/               # Capa de Infraestructura (Detalles de implementación)
│   ├── adapters/
│   │   └── postgres/             # Adaptador de persistencia PostgreSQL / JPA
│   │       ├── entities/         # Entidades JPA (SolicitudEntity, EventoSolicitudEntity, CargaMasivaEntity, DetalleCargaErrorEntity)
│   │       ├── persistence/      # Implementación de adaptadores (SolicitudRepositoryAdapter, CargaMasivaRepositoryAdapter, FolioGeneratorAdapter)
│   │       ├── repositories/     # Interfaces Spring Data JPA
│   │       └── specifications/   # Consultas dinámicas JPA Specifications
│   ├── config/                   # Configuración de Spring Beans y Transacciones (@Transactional)
│   └── entrypoints/
│       ├── controllers/          # Controladores REST (SolicitudController, CargaMasivaController) y ExceptionHandlerController (@RestControllerAdvice)
│       ├── dto/                  # DTOs de Entrada/Salida encapsulados como Java Records
│       └── validation/           # Validadores personalizados (Módulo 11 RUT)
└── shared/                       # Utilidades transversales puras (RutUtils)
```

---

## 2. Contrato de la API REST Completo

### A. Gestión de Solicitudes Manuales y Transiciones (Parte 1)

| Método | Endpoint | Status Válido | Status de Error | Descripción |
|---|---|---|---|---|
| `POST` | `/api/v1/solicitudes` | **201 Created** | 400 | Crear solicitud manual en estado `BORRADOR` con asignación de folio único correlativo (`DEV-AAAA-NNNNNN`). |
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

### B. Carga Masiva de Solicitudes CSV (Parte 2)

| Método | Endpoint | Status Válido | Status de Error | Descripción |
|---|---|---|---|---|
| `POST` | `/api/v1/cargas` | **201 Created** | 400 | Carga masiva de archivo CSV (`multipart/form-data`, parámetro `file`). Procesa las solicitudes en batch. |
| `GET` | `/api/v1/cargas/{id}` | **200 OK** | 404 | Consultar resumen del procesamiento del lote (total, OK, rechazadas, reporte de errores por fila). |

---

## 3. Exigencias de la Carga Masiva (Parte 2)

| Exigencia | Solución e Implementación Técnica |
|---|---|
| **Procesamiento por Lotes (Batching)** | Implementado agrupando iteraciones en **chunks de 100 filas**. Cada 100 entidades válidas procesadas se ejecuta `entityManager.flush()` para transmitir la ráfaga de `INSERT` multi-values a PostgreSQL en 1 viaje de red (*round-trip*), seguido de `entityManager.clear()` para liberar las referencias del Heap de la JVM (Garbage Collector). Se diferencia del enfoque fila a fila en que reduce dramáticamente la latencia de red y el sobrecosto de I/O. |
| **Tolerancia a Filas Malas** | Si una fila contiene errores (RUT inválido según Módulo 11, monto negativo o $> 10.000.000$ CLP, campos vacíos o referencia bancaria duplicada), **la carga no se aborta**. Se registra una entrada detallada en `detalles_carga_error` (`numeroFila`, `campo`, `motivo`) e incrementa `filasRechazadas`. El resto del archivo continúa procesándose normalmente. |
| **Idempotencia** | Se utiliza `referencia_banco` como llave de idempotencia. Antes de insertar una fila, se verifica en la base de datos y dentro del set del mismo archivo CSV. Si la referencia ya existe, la fila es rechazada con motivo *"Referencia bancaria duplicada (idempotencia)"*, evitando duplicar solicitudes. |
| **Transaccionalidad Definida** | La transacción guarda el registro de la `CargaMasiva` en estado `PROCESANDO`. Al finalizar todas las filas, persiste las solicitudes válidas, la lista de errores y actualiza el resumen a `COMPLETADO`. Si el proceso muriera por falla catastrófica (ej. caída de energía), la carga queda en estado `ERROR` y las solicitudes no confirmadas se revierten de forma atómica. |
| **Solicitudes Resultantes** | Nacen directamente en estado **`EN_REVISION`** con `origen = CARGA_MASIVA` y su evento de auditoría inicial (`EN_REVISION -> EN_REVISION`) especificando en el comentario: `"Creación automática desde Carga Masiva (Archivo: [nombreArchivo])"`. |
| **Bonus — Diseño Asíncrono (50.000 Filas)** | Para manejar archivos extremadamente grandes sin bloquear la conexión HTTP, el diseño ideal desacopla la carga: `POST /api/v1/cargas` recibe el archivo, lo guarda en un almacenamiento (ej. S3 / volumen local), crea el registro `CargaMasiva(estado="PROCESANDO")` y retorna inmediatamente **`202 Accepted`** con el header `Location: /api/v1/cargas/{id}`. Un worker asíncrono (`@Async` o Spring Batch / RabbitMQ) procesa el archivo en segundo plano, actualizando el progreso y permitiendo al frontend consultar el estado mediante polling en `GET /api/v1/cargas/{id}`. |

---

## 4. Justificación de Decisiones de Diseño y Exigencias Técnicas

### ¿Por qué las transiciones son acciones HTTP `POST /{id}/accion` y no un `PUT` del campo `estado`?
Un `PUT` en REST representa la sustitución completa del estado de un recurso. Sin embargo, en un dominio guiado por una **Máquina de Estados de Negocio**, una transición no es un cambio arbitrario. Cada transición representa un **comando explícito con reglas y efectos secundarios inseparables**:
- Verificación del estado de origen permitido (Regla R1).
- Autorización y restricción de roles (Regla R2).
- Validaciones específicas de la acción, como el motivo obligatorio en rechazo (R3) o la separación de funciones (R7).
- La inserción **obligatoria, inmutable y atómica** de un evento en el histórico `EventoSolicitud` (Regla R6).

### Generación de Folios Unificada $O(1)$
Se implementó el adaptador `FolioGeneratorAdapter` respaldado por la secuencia PostgreSQL `seq_folio_solicitud`. Tanto la creación manual como la carga masiva comparten este servicio para obtener números correlativos formateados (`DEV-AAAA-NNNNNN`), garantizando unicidad matemática y eliminando por completo cualquier consulta N+1.

### Máquina de Estados Cohesiva (`EstadoSolicitud.java`)
Toda la lógica de transición y sus restricciones vive encapsulada en el Enum `EstadoSolicitud.java` mediante el método `validarTransicionHacia(nuevoEstado)`.

### Manejo Global de Errores (`@ControllerAdvice`)
El componente `ExceptionHandlerController` captura todas las excepciones de dominio y validación, retornando un contrato único de error estructurado (`timestamp`, `status`, `error`, `detalle`, `path`).

---

## 5. Instrucciones para Ejecutar las Pruebas Unitarias

Toda la suite de pruebas unitarias y de integración puede ejecutarse desde la carpeta `backend` con los siguientes comandos:

```bash
cd backend
./mvnw test
```

### Resultados de la Suite de Pruebas:
- **`37/37 tests pasados (BUILD SUCCESS)`** comprobando:
  - Reglas de la Máquina de Estados (`EstadoSolicitudTest`).
  - Creación manual, idempotencia y edición en borrador con folio secuencial (`GestionarSolicitudesBorradorUseCaseTest`).
  - Transiciones de envío y anulación desde borrador (`GestionarTransicionesBorradorUseCaseTest`).
  - Transiciones de aprobación, rechazo y separación de funciones R7 (`GestionarTransicionesRevisionUseCaseTest`).
  - Transiciones de pago y límite de reapertura R4 (`GestionarTransicionesFinalesUseCaseTest`).
  - Búsqueda por ID, paginación con filtros e historial de auditoría (`ConsultarSolicitudesUseCaseTest`).
  - Carga masiva CSV, tolerancia a errores, idempotencia y solicitudes en `EN_REVISION` (`ProcesarCargaMasivaUseCaseTest`).
  - Verificación del contexto global de Spring Boot (`ApiApplicationTests`).
