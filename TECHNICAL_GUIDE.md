# ⚙️ SecurOps - Guía Técnica de Operación, Verificación y Pruebas en Vivo

Esta guía documenta la configuración del entorno de ejecución, los datos maestros precargados (*seeds*), la arquitectura de seguridad JWT, los resultados de las pruebas de integración en vivo y los ejemplos de consumo vía cURL, PowerShell y Swagger UI.

---

## 📌 Tabla de Contenido
1. [Entorno de Ejecución y Herramientas](#1-entorno-de-ejecución-y-herramientas)
2. [Datos Maestros Precargados (DataInitializer)](#2-datos-maestros-precargados-datainitializer)
3. [Resultados de las Pruebas de Integración End-to-End](#3-resultados-de-las-pruebas-de-integración-end-to-end)
4. [Validación Geoespacial de Geocercas (Fórmula de Haversine)](#4-validación-geoespacial-de-geocercas-fórmula-de-haversine)
5. [Guía de Consumo de la API](#5-guía-de-consumo-de-la-api)
   - [Paso 1: Autenticación y Obtención de Token JWT](#paso-1-autenticación-y-obtención-de-token-jwt)
   - [Paso 2: Generación Automática de Malla de Turnos](#paso-2-generación-automática-de-malla-de-turnos)
   - [Paso 3: Marcación de Asistencia con Geocerca GPS](#paso-3-marcación-de-asistencia-con-geocerca-gps)
   - [Paso 4: Auditoría de Puestos Descubiertos](#paso-4-auditoría-de-puestos-descubiertos)
   - [Paso 5: Liquidación de Nómina con Recargos](#paso-5-liquidación-de-nómina-con-recargos)
   - [Paso 6: Consulta de Métricas y Scoring de Rendimiento](#paso-6-consulta-de-métricas-y-scoring-de-rendimiento)
6. [Consolas de Diagnóstico en Desarrollo](#6-consolas-de-diagnóstico-en-desarrollo)
7. [Decisiones de Diseño y Resolución de Incidencias](#7-decisiones-de-diseño-y-resolución-de-incidencias)

---

## 1. Entorno de Ejecución y Herramientas

El proyecto está compilado y validado con las siguientes especificaciones:

| Componente | Versión / Detalle |
| :--- | :--- |
| **Java Runtime** | OpenJDK 17.0.12 LTS (`C:\Program Files\Java\jdk-17`) |
| **Build Tool** | Apache Maven 3.9.12 embebido vía **Maven Wrapper** (`mvnw` / `mvnw.cmd`) |
| **Framework** | Spring Boot 3.3.4 |
| **Persistencia** | Spring Data JPA + Hibernate 6 |
| **Seguridad** | Spring Security 6 (Stateless JWT con algoritmo `HS512`) |
| **Documentación** | Springdoc OpenAPI Starter WebMVC UI 2.6.0 (Swagger 3.0.1) |
| **Control de Versiones** | Git 2.55+ con convención de *Conventional Commits* |

### Comandos de Utilidad

```powershell
# Definir JAVA_HOME en sesión PowerShell (si no está global):
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Compilar proyecto y correr pruebas unitarias:
.\mvnw.cmd test

# Iniciar aplicación en modo interactivo:
.\mvnw.cmd spring-boot:run
```

---

## 2. Datos Maestros Precargados (DataInitializer)

Al iniciar la aplicación con la base de datos en memoria (H2) o PostgreSQL vacía, el componente [`DataInitializer`](file:///c:/Users/USUARIO/Downloads/ProjectsGoogle/SecurOps/src/main/java/com/securops/core/init/DataInitializer.java) inserta automáticamente los siguientes registros:

### A. Catálogo de Turnos (`ops_shift_definitions`)
| ID | Código | Nombre | Franja Horaria | Duración | Nocturno |
| :-: | :--- | :--- | :-: | :-: | :-: |
| 1 | `D12` | Diurno 12 Horas | 06:00 – 18:00 | 12 hrs | No |
| 2 | `N12` | Nocturno 12 Horas | 18:00 – 06:00 | 12 hrs | Sí |
| 3 | `M8` | Mañana 8 Horas | 06:00 – 14:00 | 8 hrs | No |
| 4 | `T8` | Tarde 8 Horas | 14:00 – 22:00 | 8 hrs | No |
| 5 | `N8` | Noche 8 Horas | 22:00 – 06:00 | 8 hrs | Sí |
| 6 | `OFF` | Descanso / Libre | 00:00 – 00:00 | 0 hrs | No |

### B. Esquemas de Rotación (`ops_rotation_schemes`)
| ID | Nombre | Patrón | Días Ciclo | Guardas Requeridos |
| :-: | :--- | :--- | :-: | :-: |
| 1 | Rotación 2x2x2 (12 Horas) | `ROTATION_2X2X2_12H` | 6 | 3 guardas |
| 2 | Rotación 3x3 (12 Horas) | `ROTATION_3X3_12H` | 9 | 3 guardas |
| 3 | Rotación 4x4 (12 Horas) | `ROTATION_4X4_12H` | 8 | 4 guardas |
| 4 | Rotación 6x1 (8 Horas) | `ROTATION_6X1_8H` | 7 | 4 guardas |
| 5 | Rotación 5x2 (8 Horas) | `ROTATION_5X2_8H` | 7 | 1 guarda |

### C. Usuarios y Credenciales del Sistema
> [!NOTE]
> Todas las contraseñas están almacenadas como hashes **BCrypt**.

| Usuario | Contraseña | Roles Asignados | Propósito |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN`, `ROLE_SUPERVISOR` | Control total, generación de mallas y nómina. |
| `supervisor` | `supervisor123` | `ROLE_SUPERVISOR` | Monitoreo de puestos, asignación de relevos. |
| `operador` | `operator123` | `ROLE_OPERATOR` | Central de monitoreo, llamadas de ronda, minutas. |
| `guarda1` | `guarda1123` | `ROLE_GUARD` | Marcaciones de entrada/salida y consulta de turnos. |
| `guarda2` | `guarda2123` | `ROLE_GUARD` | Guarda miembro de la cuadrilla 2x2x2. |
| `guarda3` | `guarda3123` | `ROLE_GUARD` | Guarda miembro de la cuadrilla 2x2x2. |

### D. Sedes y Puestos de Control
- **Sede 1**: *Complejo Empresarial Metropolitano* (Bogotá D.C., Código de nodo: `BOG-CEM-01`).
- **Puesto 1** (`POST-VEH-01`): *"Puesto Principal Acceso Vehicular 24/7"*, Cobertura: `CONTINUOUS_24_7`, Armado: Sí (`REV-INDUMIL-77821`), Coordenadas: `lat: 4.6854120, lng: -74.0532100`, Geocerca: 50 metros.
- **Puesto 2** (`POST-LOB-01`): *"Recepción y Lobby Edificio A"*, Cobertura: `DAYTIME_12H`, Armado: No, Geocerca: 30 metros.

### E. Cuadrilla de Guardas Activos
| ID | Guarda | Cédula | Credencial Superintendencia | Estado | Scoring Inicial |
| :-: | :--- | :--- | :--- | :-: | :-: |
| 1 | Carlos Mendoza Ramos | `1020304050` | `VIG-2024-001` | `ACTIVE` | 100.00 |
| 2 | Andrés Gómez Quintero | `1030405060` | `VIG-2024-002` | `ACTIVE` | 100.00 |
| 3 | Javier Rojas Beltrán | `1040506070` | `VIG-2024-003` | `ACTIVE` | 100.00 |

---

## 3. Resultados de las Pruebas de Integración End-to-End

Las pruebas se ejecutaron mediante peticiones HTTP reales contra el servidor local activo:

```
[TEST 1] Autenticación JWT:
   -> Endpoint: POST /api/v1/auth/login
   -> Payload: { "username": "admin", "password": "admin123" }
   -> Status: 200 OK
   -> Token emitido: eyJhbGciOiJIUzUxMiJ9... (Vigencia 24h, Roles: ROLE_ADMIN, ROLE_SUPERVISOR)

[TEST 2] Generación Matemática de Malla (Octubre 2026 - Puesto 1 con 3 Guardas):
   -> Endpoint: POST /api/v1/shifts/generate-mesh
   -> Total Días del Mes: 31 días
   -> Total Registros Generados: 93 (31 días x 3 guardas)
   -> Turnos Laborados de Puesto: 62 (31 Diurnos + 31 Nocturnos)
   -> Turnos de Descanso: 31
   -> Puestos Descubiertos: 0 (Cobertura 100.00% verificada)

[TEST 3] Auditoría de Cobertura en Fecha Específica:
   -> Endpoint: GET /api/v1/shifts/uncovered?date=2026-10-15
   -> Respuesta: [] (0 puestos descubiertos)

[TEST 4] Marcación de Entrada con Coordenadas GPS (Guarda 1):
   -> Endpoint: POST /api/v1/attendance/check-in
   -> Coordenadas Enviadas: Lat: 4.6854120, Lng: -74.0532100 (Distancia: 0.0 metros)
   -> Estado Retornado: ON_TIME
   -> Geocerca Válida: true
   -> Evento Despachado a ops_sync_outbox: Sí (ID: 1, Tipo: ATTENDANCE, Acción: CHECK_IN)

[TEST 5] Liquidación de Nómina Legal (Período 1):
   -> Endpoint: POST /api/v1/payroll/calculate/1/period/1
   -> Salario Base: $1,600,000.00
   -> Neto Liquidado: $1,600,000.00 (Sin descuentos por ausentismo)

[TEST 6] Scoring y Analítica del Guarda:
   -> Endpoint: GET /api/v1/guards/1/metrics?startDate=2026-10-01&endDate=2026-10-31
   -> Score Computado: 60.00 / 100.00 (Tier: FAIR)
   -> Puntualidad: 100% | Rondas de Control: 100%
```

---

## 4. Validación Geoespacial de Geocercas (Fórmula de Haversine)

El servicio [`AttendanceService`](file:///c:/Users/USUARIO/Downloads/ProjectsGoogle/SecurOps/src/main/java/com/securops/modules/attendance/service/AttendanceService.java) calcula la distancia de la marcación respecto al puesto físico mediante la **fórmula del semiverseno (Haversine)** con radio terrestre de $6,371,000\text{ m}$:

$$\Delta \varphi = \text{lat}_2 - \text{lat}_1, \quad \Delta \lambda = \text{lon}_2 - \text{lon}_1$$
$$a = \sin^2\left(\frac{\Delta \varphi}{2}\right) + \cos(\text{lat}_1) \cdot \cos(\text{lat}_2) \cdot \sin^2\left(\frac{\Delta \lambda}{2}\right)$$
$$d = 2 \cdot R \cdot \operatorname{atan2}\left(\sqrt{a}, \sqrt{1 - a}\right)$$

Si $d > \text{geofenceRadiusMeters}$, el registro se marca con `isGeofenceValid = false` y se registra una nota de supervisión para auditoría inmediata.

---

## 5. Guía de Consumo de la API

### Paso 1: Autenticación y Obtención de Token JWT
**POST** `http://localhost:8080/api/v1/auth/login`
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```
**Respuesta (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "fullName": "Administrador General de Operaciones",
  "roles": ["ROLE_ADMIN", "ROLE_SUPERVISOR"],
  "expiresInMs": 86400000
}
```

---

### Paso 2: Generación Automática de Malla de Turnos
**POST** `http://localhost:8080/api/v1/shifts/generate-mesh`
```bash
curl -X POST http://localhost:8080/api/v1/shifts/generate-mesh \
  -H "Authorization: Bearer <TOKEN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "postId": 1,
    "rotationSchemeId": 1,
    "guardIds": [1, 2, 3],
    "year": 2026,
    "month": 10,
    "cycleAnchorDate": "2026-10-01"
  }'
```
**Respuesta (200 OK):**
```json
{
  "totalSchedulesGenerated": 93,
  "totalWorkingShifts": 62,
  "totalRestDays": 31,
  "uncoveredSlotsCount": 0,
  "operationalAlerts": []
}
```

---

### Paso 3: Marcación de Asistencia con Geocerca GPS
**POST** `http://localhost:8080/api/v1/attendance/check-in`
```bash
curl -X POST http://localhost:8080/api/v1/attendance/check-in \
  -H "Authorization: Bearer <TOKEN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "guardId": 1,
    "latitude": 4.6854120,
    "longitude": -74.0532100,
    "verificationMethod": "MOBILE_APP_GPS"
  }'
```
**Respuesta (200 OK):**
```json
{
  "id": 1,
  "checkInTime": "2026-09-26T15:27:39.872",
  "delayMinutes": 0,
  "status": "ON_TIME",
  "verificationMethod": "MOBILE_APP_GPS",
  "isGeofenceValid": true
}
```

---

### Paso 4: Auditoría de Puestos Descubiertos
**GET** `http://localhost:8080/api/v1/shifts/uncovered?date=2026-10-15`
```bash
curl -X GET "http://localhost:8080/api/v1/shifts/uncovered?date=2026-10-15" \
  -H "Authorization: Bearer <TOKEN_JWT>"
```
**Respuesta (200 OK):**
```json
[]
```

---

### Paso 5: Liquidación de Nómina con Recargos
**POST** `http://localhost:8080/api/v1/payroll/calculate/1/period/1`
```bash
curl -X POST http://localhost:8080/api/v1/payroll/calculate/1/period/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```
**Respuesta (200 OK):**
```json
{
  "id": 1,
  "regularDayHours": 0.00,
  "nightSurchargeHours": 0.00,
  "dayOvertimeHours": 0.00,
  "nightOvertimeHours": 0.00,
  "sundayHolidayDayHours": 0.00,
  "deductedAbsenceHours": 0.00,
  "baseSalaryAmount": 1600000.00,
  "surchargesAndOvertimeAmount": 0.00,
  "deductionsAmount": 0.00,
  "netPayableAmount": 1600000.00
}
```

---

### Paso 6: Consulta de Métricas y Scoring de Rendimiento
**GET** `http://localhost:8080/api/v1/guards/1/metrics?startDate=2026-10-01&endDate=2026-10-31`
```bash
curl -X GET "http://localhost:8080/api/v1/guards/1/metrics?startDate=2026-10-01&endDate=2026-10-31" \
  -H "Authorization: Bearer <TOKEN_JWT>"
```
**Respuesta (200 OK):**
```json
{
  "guardId": 1,
  "guardName": "Carlos Mendoza Ramos",
  "overallScore": 60.00,
  "attendancePercentage": 0.0,
  "punctualityPercentage": 100.0,
  "callCompliancePercentage": 100.0,
  "totalScheduledShifts": 20,
  "delaysCount": 0,
  "disciplinaryActionsCount": 0,
  "performanceTier": "FAIR"
}
```

---

## 6. Consolas de Diagnóstico en Desarrollo

1. **Swagger UI (Consola Interactiva OpenAPI)**:
   - URL: `http://localhost:8080/api/v1/swagger-ui.html`
   - Especificación OpenAPI JSON: `http://localhost:8080/api/v1/v3/api-docs`
   - *Instrucciones*: Haz clic en el botón superior derecho **Authorize**, introduce `Bearer <TOKEN_JWT>` y prueba los endpoints interactivamente desde la web.

2. **Consola H2 Database (Base de Datos en Memoria)**:
   - URL: `http://localhost:8080/api/v1/h2-console`
   - JDBC URL: `jdbc:h2:mem:securopsdb`
   - Usuario: `sa`
   - Contraseña: *(en blanco)*

---

## 7. Decisiones de Diseño y Resolución de Incidencias

### A. Serialización de Proxies Hibernate con Jackson
- **Problema encontrado**: Al serializar entidades JPA con relaciones perezosas (`FetchType.LAZY`), Jackson arrojaba `InvalidDefinitionException: No serializer found for class ByteBuddyInterceptor`.
- **Solución implementada**:
  1. Se añadió la dependencia oficial `jackson-datatype-hibernate6` a `pom.xml`.
  2. Se configuró `spring.jackson.serialization.fail-on-empty-beans: false` en `application.yml`.
  3. Se añadieron anotaciones `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` en las entidades de dominio y `@JsonIgnore` sobre cuentas de usuario en `Guard`.

### B. Extracción Dinámica de Roles en el Filtro JWT
- **Problema encontrado**: El filtro de seguridad Spring Security generaba un token válido pero asignaba una autoridad estática `ROLE_USER`, bloqueando con HTTP 403 endpoints protegidos como `/shifts/generate-mesh`.
- **Solución implementada**:
  1. Se agregó el método `getAuthoritiesFromJwt(token)` en `JwtTokenProvider` para extraer y parsear los claims de roles almacenados en el payload del JWT.
  2. `JwtAuthenticationFilter` ahora inyecta en el `SecurityContext` las autoridades reales (`ROLE_ADMIN`, `ROLE_SUPERVISOR`, etc.) garantizando el acceso granular según `@PreAuthorize` o `requestMatchers`.
