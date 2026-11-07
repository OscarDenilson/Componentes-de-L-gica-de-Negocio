# Matriz de trazabilidad
| Requisito/Regla | Historia | Dominio/caso de uso | API | Prueba/evidencia |
|---|---|---|---|---|
| BR-01 datos obligatorios e intervalo | HU-04 | Appointment constructor + validInterval | POST /api/appointments | AppointmentTest.rejectsInvalidInterval |
| BR-03 no solapamiento | HU-03, HU-05 | AppointmentService.create + repository overlap query | POST /api/appointments | Prueba de integración pendiente con PostgreSQL |
| BR-04 confirmar cita | HU-06 | Appointment.confirm / State Machine | POST /{id}/confirm | AppointmentTest.confirmationRequiresRequested |
| BR-05 registrar tardanza | HU-07 | Appointment.registerArrival | POST /{id}/arrival | AppointmentTest.latenessDoesNotAutoDecide |
| BR-06 decisión médica explícita | HU-08, HU-09 | Appointment.decide | POST /{id}/decision | assignedDoctorCanDecide, otherDoctorCannotDecide |
| BR-08 cancelación trazable | HU-10, HU-20 | Appointment.cancel + DomainEvent | POST /{id}/cancel | cancellationEmitsEvent |
| BR-09 no-show | HU-11 | Appointment.markNoShow | POST /{id}/no-show | completar prueba de integración |
| BR-07 autorización aseguradora | HU-12, HU-19 | authorizationSatisfied Specification | POST /api/appointments | specificationsCombineAndNot |
| FR-12 historial | HU-14 | pendiente en aplicación | pendiente | pendiente |
| FR-13 indicadores | HU-15 | pendiente en aplicación | pendiente | pendiente |
| FR-11 resultado clínico | HU-13 | pendiente en aplicación | pendiente | pendiente |

**Lectura honesta:** esta matriz identifica componentes implementados y brechas. No declarar la solución completa para historial, indicadores, resultado clínico ni CI/despliegue activo hasta implementar, probar y publicar esos componentes.
