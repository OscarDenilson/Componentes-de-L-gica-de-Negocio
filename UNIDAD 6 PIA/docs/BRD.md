# BRD — MediNova

## Resumen y contexto
MediNova es una clínica privada con tres sucursales y aproximadamente 35 médicos, algunos empleados y otros por honorarios. Ofrece medicina general, especialidades, estudios básicos y procedimientos menores. Los pacientes pagan directamente o mediante convenios con aseguradoras.

## Problema de negocio
La agenda compartida y las tareas manuales de recepción favorecen duplicidades, poca visibilidad entre sucursales, evidencia inconsistente de cancelaciones y dificultad para medir no-shows e historial. Los horarios médicos cambian semanalmente.

## Objetivos
- OBJ-01 Reducir duplicidades mediante validación de disponibilidad.
- OBJ-02 Mejorar visibilidad por médico, especialidad y sucursal.
- OBJ-03 Mantener trazabilidad de eventos de cita.
- OBJ-04 Gestionar tardanzas, cancelaciones, no-shows y reagendas.
- OBJ-05 Registrar resultado de consulta y seguimiento.
- OBJ-06 Obtener indicadores operativos.

## Actores
Paciente; recepcionista; médico; personal administrativo; aseguradora.

## Alcance
Incluye pacientes, disponibilidad, citas, confirmaciones, llegadas, tardanzas, no-shows, cancelaciones/reagendas, autorizaciones de aseguradora, resultados de consulta, historial e indicadores. Fuera del alcance inicial: políticas definitivas de sobrecupo, reincidencia de no-shows, permisos formales, ventanas de cancelación y límites financieros; requieren validación.

## Requerimientos funcionales
- FR-01 Registrar y localizar pacientes por teléfono o expediente.
- FR-02 Consultar disponibilidad por médico, especialidad y sucursal.
- FR-03 Agendar cita con paciente, médico, especialidad, sucursal, horario y motivo.
- FR-04 Evitar solapamientos de citas activas de un médico.
- FR-05 Registrar intento de confirmación aproximadamente un día antes.
- FR-06 Registrar llegada y calcular minutos de tardanza.
- FR-07 Permitir decisión médica de atender, esperar o reprogramar; nunca decidir automáticamente solo por puntualidad.
- FR-08 Registrar cancelaciones y motivo.
- FR-09 Registrar no-show ligado a cita y paciente.
- FR-10 Representar autorización previa de aseguradora cuando aplique.
- FR-11 Registrar resultado de consulta: receta, estudios o nueva cita.
- FR-12 Consultar historial de citas del paciente.
- FR-13 Producir indicadores de citas, cancelaciones, no-shows, duplicados y disponibilidad.
- FR-14 Mantener eventos auditables de cambios relevantes.
- FR-15 Mantener disponibilidad asociada a médico, especialidad y sucursal.

## Reglas de negocio
- BR-01 La cita debe tener paciente, médico, sucursal, especialidad, horario válido y motivo.
- BR-02 No se permiten intervalos de fin igual o anterior al inicio.
- BR-03 No se confirma una cita si el médico tiene un traslape activo.
- BR-04 Recepción intenta confirmar un día antes; el caso no define integración automática de mensajería.
- BR-05 Si el paciente llega hasta 15 minutos tarde, recepción consulta al médico; la decisión corresponde al médico.
- BR-06 Si el retraso supera 15 minutos, normalmente se ofrece reagendar, sujeto a decisión operativa y sin automatizar un rechazo clínico.
- BR-07 La autorización previa se valida solo cuando el convenio/servicio la exige.
- BR-08 La cancelación debe conservar motivo y evento.
- BR-09 No-show se asocia a la cita y al paciente.
- BR-10 Sobrecupo depende de la política del médico y límite definido; pendiente de validación.
- BR-11 No se aplica sanción automática por reincidencia de no-show; pendiente de política.
- BR-12 Resultado clínico se registra después de la atención.

## Requerimientos no funcionales
- NFR-01 Persistencia relacional PostgreSQL.
- NFR-02 API REST documentada con OpenAPI/Swagger.
- NFR-03 Validación consistente de reglas en dominio.
- NFR-04 Pruebas automatizadas y objetivo de cobertura de instrucciones ≥80%.
- NFR-05 Configuración por variables de entorno.
- NFR-06 Ejecución reproducible con Docker Compose.
- NFR-07 No incluir credenciales reales en repositorio.
- NFR-08 Proteger datos personales y clínicos; autenticación/autorización antes de producción.

## Supuestos y pendientes
El caso no especifica políticas definitivas de permisos, ventanas de cancelación, duración por especialidad, reincidencia de no-show, sobrecupo ni protocolo de autorización de aseguradora. Se modelan puntos de extensión, no decisiones institucionales finales.
