# MediNova PIA — Java 21 + Spring Boot + PostgreSQL

Proyecto académico para el caso MediNova (gestión de citas médicas). Incluye análisis, historias Gherkin, glosario, ADRs, diagramas, API REST/OpenAPI, DDD, Specification, State Machine, eventos de dominio, tests, Docker Compose y CI workflow.

## Requisitos
Java 21, Maven 3.9+, Docker Desktop/Engine y Git.

## Inicio rapido
docker compose up --build -d

## Arranque local
1. `cp .env.example .env` y cambia `DB_PASSWORD` (no subir `.env`).
2. `docker compose up -d db`
3. `mvn spring-boot:run`
4. Swagger: http://localhost:8080/swagger-ui.html; API: http://localhost:8080/api/appointments
5. Pruebas: `mvn verify` (JaCoCo aplica umbral de 80% de instrucciones; si falla, completar cobertura antes de afirmar cumplimiento).

## Docker Compose completo
`docker compose up --build -d` inicia DB y app. Para ejecutar suite en contenedor: `docker compose --profile test run --rm tests`.

## restaurar base de datos
docker compose down -v
docker compose up -d

## Flujo demo
- Crear cita con `POST /api/appointments`.
- Confirmar con `POST /api/appointments/{id}/confirm`.
- Registrar llegada con `POST /api/appointments/{id}/arrival` y `arrivedAt`.
- Verificar que la tardanza no decide automáticamente.
- Decisión médica: `POST /api/appointments/{id}/decision` con `doctorId`, `decision` (`ATENDER`, `ESPERAR`, `REPROGRAMAR`) y `notes`.
- Demostrar transiciones inválidas, cancelación y no-show.

## Trazabilidad y documentos
- `docs/BRD.md`, `docs/UserStories_Gherkin.md`, `docs/Glossary.md`
- `docs/architecture/ADRs.md`, `docs/architecture/diagrams/` (dominio, secuencias, State Machine y flujo BPMN)
- `docs/api/openapi.yaml`; Swagger UI generado por springdoc.
- `data/` contiene fixtures ficticios del paquete del alumno.
- `originales/` conserva artefactos originales del ZIP de Unidades 2/3.

## Seguridad y limitaciones
**Prototipo académico, no sistema clínico de producción.** La identidad del médico se recibe en el cuerpo de la petición y no se valida con sesión autenticada; debe reemplazarse por Spring Security/OIDC y permisos por rol. No se implementa aún el módulo clínico completo de recetas/estudios, todos los catálogos de pacientes/médicos/sucursales, ni paneles de indicadores. La comprobación de solapamiento requiere protección transaccional/índice y pruebas concurrentes antes de uso real. Use únicamente datos ficticios.

## GitHub y despliegue
Crear repositorio vacío en la cuenta del equipo, ejecutar `git init`, `git add .`, `git commit`, configurar remoto y `git push`. El workflow CI está en `.github/workflows/ci.yml`; debe verificarse una ejecución real tras el push. Para Railway/Render configure servicio Java/Docker, PostgreSQL administrado y variables `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT`; el despliegue y URL quedan pendientes de credenciales y cuenta del equipo, no se simulan aquí.

## Interfaz gráfica web

La aplicación incluye un panel web visual completo e interactivo servido automáticamente por Spring Boot en `src/main/resources/static/index.html`. Al iniciar la aplicación o Docker Compose, abre `http://localhost:8080/` (o directamente `http://localhost:8080/index.html`).

### Características de la interfaz:
- **Tablero Clínico (Dashboard):** Métricas en vivo (total de citas, citas solicitadas, confirmadas, en sala con llegada registrada y atendidas), citas activas recientes y botón de "Cita Demo" para pruebas instantáneas.
- **Gestión Integral de Citas:**
  - Búsqueda en tiempo real por texto (ID, paciente, médico, especialidad o motivo) y filtro dropdown por estado (`SOLICITADA`, `CONFIRMADA`, `LLEGADA_REGISTRADA`, `ATENDIDA`, `REQUIERE_REPROGRAMACION`, `CANCELADA`, `NO_SHOW`).
  - **Agendar Cita:** Modal con validación de horarios, motivo, sucursal, especialidad y reglas de especificación (paciente/médico/sucursal activos y autorización de aseguradora).
  - **Confirmar Cita:** Transición de `SOLICITADA` a `CONFIRMADA`.
  - **Registrar Llegada con Cálculo de Tardanza:** Modal con selector de hora o accesos rápidos ("Ahora", "+10 min", "+25 min") que calcula dinámicamente si el paciente llegó a tiempo (verde), con tolerancia (amarillo) o con tardanza excesiva (rojo).
  - **Decisión Médica (HU-08):** Modal profesional con tarjetas visuales (`ATENDER`, `ESPERAR`, `REPROGRAMAR`), validación de médico que atiende y campo para notas médicas.
  - **Cancelar Cita:** Registro estructurado del motivo de cancelación.
  - **Inasistencia (No-Show):** Registro de inasistencia del paciente tras superar el umbral de espera.
  - **Detalle de Cita:** Modal con el desglose completo de la cita, paciente, médico, fechas, estado y notas clínicas.
- **Catálogo de Datos (Operaciones y Autorización de Citas):** Catálogo integrado con el dataset institucional que relaciona directamente las citas que se pueden crear con las IDs de las operaciones (`CITA-O001` .. `CITA-O060`). Permite aplicar el **tratamiento de autorización de citas de pacientes** (autorizar/revocar estado con persistencia) y agendar citas directamente vinculadas a la operación seleccionada con autocompletado y validación de autorización.
- **Validación Estricta de Existencia de IDs:** Al intentar crear una cita con un ID de paciente, médico inexistente o no autorizado, o con una especialidad que no corresponda al médico, el sistema bloquea la creación y devuelve el mensaje de error correspondiente.
- **Módulo de Operaciones & Banco de Pruebas:**
  - **60 Operaciones con Totales Calculados:** Consulta y gestión de las 60 operaciones del dataset con cálculo de total mediante `BigDecimal` (`precio_mxn × cantidad`), detalle de líneas asociadas y control de autorización.
  - **Evaluación de Specifications DDD:** Evaluación interactiva en tiempo real de `puede_procesarse` (`actor_activo AND detalle_no_vacio AND autorizado`) y `beneficio_referencia` (`actor_preferente AND total >= 1500 AND NOT promocion`).
  - **Runner de 120 Casos de Prueba:** Ejecuta automáticamente los 120 casos de `casos_prueba.csv` y muestra la evidencia técnica calculada con semáforos de cumplimiento.
  - **Runner de 20 Pruebas de Transición:** Ejecuta los 20 casos de `transiciones_prueba.csv`, probando la State Machine, transiciones válidas (`SOLICITADA → CONFIRMADA → ATENDIDA`), cancelaciones y excepciones controladas ante transiciones inválidas.
- **Visualizador de la Máquina de Estados:** Diagrama explicativo de las transiciones y reglas de negocio del caso MediNova para defensas y presentaciones académicas.
- **Monitoreo de Conexión:** Indicador de estado de la API en tiempo real (conectado/desconectado) con auto-refresco y enlaces directos a Swagger UI (`/swagger-ui.html`) y al endpoint JSON (`/api/appointments`).

### Cómo iniciar y probar

1. Inicia los servicios con Docker Compose:
   ```powershell
   docker compose up -d --build
   ```
2. Abre tu navegador web en:
   ```
   http://localhost:8080/
   ```
3. La interfaz se conectará automáticamente a los endpoints `/api/appointments` de la API REST. Swagger UI sigue disponible en `http://localhost:8080/swagger-ui.html`.
