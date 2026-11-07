# Architecture Decision Records (ADRs)

## ADR-001 — Java 21 y Spring Boot
**Estado:** Aceptado para prototipo académico.
**Contexto:** se necesita una aplicación REST mantenible y desplegable con persistencia relacional.
**Decisión:** Java 21, Spring Boot, Spring Web, Validation y Spring Data JPA.
**Alternativas:** Node.js/Express; Python/FastAPI.
**Consecuencias:** ecosistema maduro y estructura clara; requiere JDK/Maven y pruebas de configuración.

## ADR-002 — PostgreSQL como persistencia
**Estado:** Aceptado.
**Contexto:** citas, pacientes, médicos, sucursales y eventos requieren integridad relacional.
**Decisión:** PostgreSQL 16 en Docker Compose.
**Alternativas:** MySQL, SQLite.
**Consecuencias:** relaciones y transacciones robustas; despliegue necesita credenciales y volumen persistente.

## ADR-003 — Modelo de dominio centrado en Cita
**Estado:** Aceptado.
**Contexto:** el proceso central es agendar, confirmar, registrar llegada, decidir y cerrar la cita.
**Decisión:** Appointment es la entidad/agregado que valida transiciones y emite Domain Events.
**Alternativas:** lógica concentrada en controllers; CRUD sin invariantes.
**Consecuencias:** reglas testeables fuera de la interfaz; eventos transitorios deben publicarse/persistirse formalmente en fase productiva.

## ADR-004 — Specification composable
**Estado:** Aceptado.
**Contexto:** las reglas de agendamiento combinan estado de paciente/médico/sucursal, intervalo y autorización.
**Decisión:** Specification<T> con AND/OR/NOT y especificaciones nombradas.
**Alternativas:** condicionales duplicados en endpoints.
**Consecuencias:** reglas combinables y fáciles de probar; se debe mantener trazabilidad con reglas aprobadas por negocio.

## ADR-005 — State Machine en la entidad Cita
**Estado:** Aceptado.
**Contexto:** los estados no deben cambiarse arbitrariamente.
**Decisión:** transiciones encapsuladas: SOLICITADA→CONFIRMADA; SOLICITADA/CONFIRMADA→LLEGADA_REGISTRADA; decisión médica → ATENDIDA/LLEGADA_REGISTRADA/REQUIERE_REPROGRAMACION; cancelación/no-show según estado.
**Alternativas:** permitir cualquier actualización de estado.
**Consecuencias:** rechaza transiciones inválidas y permite pruebas directas.

## ADR-006 — API REST y OpenAPI
**Estado:** Aceptado.
**Contexto:** la defensa necesita demostrar casos de uso y contrato.
**Decisión:** endpoints REST bajo /api/appointments y Swagger UI con springdoc-openapi.
**Alternativas:** interfaz web solamente.
**Consecuencias:** contrato navegable; los endpoints de demostración requieren autenticación antes de datos reales.

## ADR-007 — Compose + CI/CD
**Estado:** Aceptado.
**Contexto:** el entorno debe ser reproducible y validarse en cada push.
**Decisión:** Docker Compose para DB/app/tests y GitHub Actions para `mvn verify`.
**Alternativas:** ejecución manual local.
**Consecuencias:** automatización; CI real requiere subir el proyecto al repositorio del equipo y observar una ejecución exitosa.
