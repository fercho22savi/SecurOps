# 🏛️ SecurOps - Estructura del Proyecto y Catálogo de Archivos

Este documento describe la organización arquitectónica, distribución de paquetes y responsabilidad técnica de cada archivo dentro del ecosistema **SecurOps**.

---

## 📂 Visión General del Árbol de Directorios

```
SecurOps/
├── pom.xml                                                         # Gestión de dependencias Maven y plugins de compilación
├── PROJECT_STRUCTURE.md                                            # Catálogo exhaustivo de arquitectura y archivos
├── README.md                                                       # Presentación oficial del proyecto para GitHub
└── src/
    ├── main/
    │   ├── java/com/securops/
    │   │   ├── SecurOpsApplication.java                            # Clase principal de arranque Spring Boot (@EnableScheduling)
    │   │   └── modules/
    │   │       ├── security/                                       # Módulo de Autenticación, RBAC y Seguridad JWT
    │   │       │   ├── config/
    │   │       │   │   └── SecurityConfig.java                     # Filtros de seguridad, endpoints públicos/privados, BCrypt
    │   │       │   ├── entity/
    │   │       │   │   ├── RoleType.java                           # Enum: ROLE_ADMIN, ROLE_SUPERVISOR, ROLE_OPERATOR, ROLE_GUARD
    │   │       │   │   └── User.java                               # Entidad JPA: Cuenta de usuario y roles
    │   │       │   └── jwt/
    │   │       │       ├── JwtAuthenticationFilter.java            # Filtro OncePerRequest para interceptar tokens Bearer
    │   │       │       └── JwtTokenProvider.java                   # Emisión, firma HMAC-SHA y validación de tokens JWT
    │   │       ├── posts/                                          # Módulo de Puestos de Control y Sedes Operativas
    │   │       │   ├── entity/
    │   │       │   │   ├── SecurityPost.java                       # Entidad JPA: Puestos físicos, coordenadas GPS y geocercas
    │   │       │   │   ├── ServiceCoverageType.java                # Enum: Coberturas 24/7, 12h diurno, 12h nocturno, 8h 5x2, 8h 3x3
    │   │       │   │   └── Site.java                               # Entidad JPA: Sedes físicas, sucursales y códigos de nodo
    │   │       │   └── repository/
    │   │       │       └── SecurityPostRepository.java             # Consultas JPA para búsqueda por código, sede y estado activo
    │   │       ├── guards/                                         # Módulo de Guardas de Seguridad y Scoring
    │   │       │   ├── controller/
    │   │       │   │   └── GuardAnalyticsController.java           # Endpoint REST para analítica y scoring (/api/v1/guards)
    │   │       │   ├── dto/
    │   │       │   │   └── GuardMetricsDto.java                    # DTO con porcentaje de puntualidad, asistencia y tier
    │   │       │   ├── entity/
    │   │       │   │   ├── Guard.java                              # Entidad JPA: Datos personales, credenciales y score actual
    │   │       │   │   └── GuardStatus.java                        # Enum: ACTIVE, ON_VACATION, MEDICAL_LEAVE, SUSPENDED, INACTIVE
    │   │       │   ├── repository/
    │   │       │   │   └── GuardRepository.java                    # Búsquedas por cédula, credencial de vigilancia y estado
    │   │       │   └── service/
    │   │       │       └── GuardScoringService.java                # Motor de cálculo ponderado de rendimiento (KPIs y penalidades)
    │   │       ├── shifts/                                         # Módulo de Mallas y Generación Dinámica de Turnos
    │   │       │   ├── controller/
    │   │       │   │   └── ShiftScheduleController.java            # Endpoints REST: Generación mensual, relevos y alertas
    │   │       │   ├── engine/
    │   │       │   │   ├── RotationStrategy.java                   # Interfaz del Patrón Strategy para esquemas rotativos
    │   │       │   │   ├── TwoByTwoRotationStrategy.java           # Estrategia 2x2x2 (12h): 2D, 2N, 2L (Cobertura 24/7 con 3 guardas)
    │   │       │   │   ├── ThreeByThreeRotationStrategy.java       # Estrategia 3x3 (12h): 3D, 3N, 3L (Ciclo 9 días para 24/7)
    │   │       │   │   ├── FourByFourRotationStrategy.java         # Estrategia 4x4 (12h): 2D, 2N, 4L (Ciclo 8 días con 4 guardas)
    │   │       │   │   ├── SixByOneRotationStrategy.java           # Estrategia 6x1 (8h): 6 días laborados, 1 descanso semanal
    │   │       │   │   ├── FiveByTwoRotationStrategy.java          # Estrategia 5x2 (8h): Lunes a Viernes, fin de semana libre
    │   │       │   │   ├── ShiftMeshGenerationRequest.java         # DTO de solicitud de generación de malla
    │   │       │   │   ├── ShiftMeshGenerationResult.java          # DTO de respuesta con métricas y alertas operativas
    │   │       │   │   └── ShiftMeshGeneratorEngine.java           # Orquestador del motor de mallas y auditoría de cobertura
    │   │       │   ├── entity/
    │   │       │   │   ├── RotationPattern.java                    # Enum de patrones de rotación soportados
    │   │       │   │   ├── RotationScheme.java                     # Entidad JPA: Configuración de esquemas y tamaño de cuadrilla
    │   │       │   │   ├── ShiftDefinition.java                    # Entidad JPA: Catálogo de turnos (D12, N12, M8, T8, N8, OFF)
    │   │       │   │   ├── ShiftSchedule.java                      # Entidad JPA: Asignación guarda-puesto-fecha en la malla
    │   │       │   │   ├── ShiftScheduleStatus.java                # Enum: SCHEDULED, CONFIRMED, IN_PROGRESS, UNCOVERED, REPLACED
    │   │       │   │   └── ShiftType.java                          # Enum de tipos de jornada y descansos
    │   │       │   ├── repository/
    │   │       │   │   ├── RotationSchemeRepository.java           # Consultas por patrón de rotación
    │   │       │   │   ├── ShiftDefinitionRepository.java          # Consultas al catálogo por código o tipo de turno
    │   │       │   │   └── ShiftScheduleRepository.java            # Consultas de turnos por rango de fechas, guarda y puesto
    │   │       │   └── service/
    │   │       │       └── ShiftManagementService.java             # Lógica de negocio: generación de mallas y asignación de relevos
    │   │       ├── attendance/                                     # Módulo de Marcaciones, Asistencia y Biometría
    │   │       │   ├── entity/
    │   │       │   │   ├── AttendanceRecord.java                   # Entidad JPA: Reloj checador, retardo en minutos y GPS
    │   │       │   │   ├── AttendanceStatus.java                   # Enum: ON_TIME, DELAYED, EARLY_LEAVE, ABSENT, JUSTIFIED_ABSENCE
    │   │       │   │   └── VerificationMethod.java                 # Enum: BIOMETRIC_DEVICE, MOBILE_APP_GPS, CONTROL_ROOM_CALL
    │   │       │   └── repository/
    │   │       │       └── AttendanceRecordRepository.java         # Consultas de asistencias y cálculo de demoras
    │   │       ├── monitoring/                                     # Módulo de Central de Monitoreo, Rondas y Novedades
    │   │       │   ├── entity/
    │   │       │   │   ├── CallStatus.java                         # Enum: ON_TIME, DELAYED, MISSED, ALERT_RAISED
    │   │       │   │   ├── ControlCallLog.java                     # Entidad JPA: Llamadas de ronda y minutas de control
    │   │       │   │   ├── NoveltyRecord.java                      # Entidad JPA: Novedades operativas, incapacidades y sanciones
    │   │       │   │   └── NoveltyType.java                        # Enum: MEDICAL_LEAVE, UNJUSTIFIED_ABSENCE, EMERGENCY_RELIEF, etc.
    │   │       │   └── repository/
    │   │       │       ├── ControlCallLogRepository.java           # Auditoría de llamadas de verificación por puesto y guarda
    │   │       │       └── NoveltyRecordRepository.java            # Consulta de novedades que impactan nómina o scoring
    │   │       ├── payroll/                                        # Módulo de Nómina y Liquidación Operativa
    │   │       │   ├── controller/
    │   │       │   │   └── PayrollController.java                  # Endpoint REST para liquidar nómina (/api/v1/payroll)
    │   │       │   ├── entity/
    │   │       │   │   ├── PayrollPeriod.java                      # Entidad JPA: Períodos quincenales o mensuales
    │   │       │   │   └── PayrollSettlement.java                  # Entidad JPA: Desglose de horas ordinarias, extras y recargos
    │   │       │   ├── repository/
    │   │       │   │   ├── PayrollPeriodRepository.java            # Búsqueda de períodos por código
    │   │       │   │   └── PayrollSettlementRepository.java        # Consulta de liquidaciones por guarda y período
    │   │       │   └── service/
    │   │       │       └── PayrollCalculatorService.java           # Algoritmo de liquidación legal (HED, HEN, festivos y descuentos)
    │   │       └── sync/                                           # Módulo de Sincronización Resiliente Multisitio
    │   │           ├── controller/
    │   │           │   └── SyncController.java                     # Endpoint REST para ingesta de lotes (/api/v1/sync/ingest)
    │   │           ├── entity/
    │   │           │   ├── IdempotencyStore.java                   # Entidad JPA: Registro de tokens UUID para evitar duplicados
    │   │           ├── SyncEventOutbox.java                    # Entidad JPA: Cola transaccional de eventos local (Transactional Outbox)
    │   │           │   └── SyncStatus.java                         # Enum: PENDING, IN_FLIGHT, SYNCED, FAILED, CONFLICT_RESOLVED
    │   │           ├── repository/
    │   │           │   ├── IdempotencyStoreRepository.java         # Validación de duplicados por clave de idempotencia
    │   │           │   └── SyncEventOutboxRepository.java          # Consulta de eventos pendientes ordenados cronológicamente
    │   │           └── service/
    │   │               └── MultisiteSyncService.java               # Worker en segundo plano (@Scheduled) y despachador tolerante a fallos
    │   └── resources/
    │       ├── application.yml                                     # Configuración de puerto, base de datos H2/PostgreSQL y claves JWT
    │       └── db/migration/
    │           └── V1__init_schema.sql                             # DDL relacional con índices, llaves foráneas y restricciones
    └── test/
        └── java/com/securops/modules/shifts/engine/
            └── RotationStrategyTest.java                           # Pruebas unitarias de cobertura 24/7 y no solapamiento
```

---

## 🔍 Detalle por Capas y Patrones de Diseño

### 1. Capa de Dominio y Entidades (`entity/`)
- Mapeo relacional con **Jakarta Persistence (JPA)** e **Hibernate**.
- Manejo de **bloqueo optimista** (`@Version`) en `ShiftSchedule` para evitar carreras entre reasignaciones concurrentes.
- Índices compuestos estratégicos en columnas de alta cardinalidad (`guard_id, shift_date`, `security_post_id, shift_date`, `status, created_at`).

### 2. Capa de Acceso a Datos (`repository/`)
- Basada en **Spring Data JPA**.
- Métodos derivados y consultas JPQL optimizadas para la evaluación rápida de mallas de turnos, puntualidad y cumplimiento de llamadas de control.

### 3. Capa del Motor Algorítmico (`shifts/engine/`)
- **Patrón Strategy**: Encapsula cada modelo de rotación (`TwoByTwoRotationStrategy`, `ThreeByThreeRotationStrategy`, etc.) bajo la interfaz común `RotationStrategy`.
- **Inversión de Control**: `ShiftMeshGeneratorEngine` inyecta automáticamente todas las estrategias registradas como componentes de Spring, permitiendo agregar nuevos esquemas sin modificar el código base (principio *Open/Closed* de SOLID).
- **Validación Automática de Cobertura**: Si una cuadrilla no tiene suficientes guardas para garantizar el 100% del servicio, el motor detecta el déficit, crea registros con estado `UNCOVERED` y emite alertas de forma preventiva.

### 4. Capa de Sincronización Resiliente (`sync/`)
- **Transactional Outbox Pattern**: Toda operación crítica en una sede local (marcación de entrada, llamada de verificación, novedad) se guarda en la base de datos local y, dentro de la misma transacción ACID, se genera un registro en `ops_sync_outbox`.
- **Scheduled Dispatcher**: Un proceso en segundo plano despacha los eventos pendientes hacia la sede central. Si se produce una pérdida de conectividad, el worker reintenta con backoff exponencial.
- **Idempotent Ingestion**: La sede central procesa los eventos asegurando que ningún UUID sea aplicado más de una vez mediante `IdempotencyStore`.

### 5. Capa de Liquidación de Nómina (`payroll/`)
- **Desglose legal de jornada**: Separa la jornada ordinaria diurna de la nocturna (21:00 a 06:00), computa horas extras (HED a 1.25x, HEN a 1.75x) y recargos dominicales/festivos diurnos (1.75x) y nocturnos (2.10x).
- **Cruce con Novedades**: Aplica deducciones exactas por ausentismo no justificado y permisos no remunerados.

### 6. Capa de Seguridad y RBAC (`security/`)
- **Spring Security 6** con arquitectura **Stateless**.
- **Filtro JWT**: Valida la firma criptográfica HMAC-SHA de cada petición entrante y extrae roles para la autorización basada en anotaciones (`@PreAuthorize`).
