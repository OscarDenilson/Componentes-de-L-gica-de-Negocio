# Historias de usuario y criterios de aceptación Gherkin

Historias derivadas del BRD, entrevistas simuladas, BPMN y glosario de MediNova. Las políticas no definidas en el caso se marcan como pendientes de validación con el stakeholder.

## HU-01 — Registrar paciente
**Historia:** Como recepcionista quiero registrar un paciente nuevo para gestionar sus citas.

```gherkin
Feature: Registrar paciente
  Scenario: Camino válido
    Dado que se capturan nombre, teléfono, fecha de nacimiento y correo válidos, cuando guardo, entonces el paciente queda registrado.

  Scenario: Validación o excepción
    Dado que falta un dato obligatorio, cuando guardo, entonces el sistema rechaza la solicitud.
```

## HU-02 — Localizar paciente
**Historia:** Como recepcionista quiero buscar pacientes por teléfono o expediente para evitar duplicados.

```gherkin
Feature: Localizar paciente
  Scenario: Camino válido
    Dado un teléfono o expediente existente, cuando busco, entonces aparece el paciente coincidente.

  Scenario: Validación o excepción
    Dado que no hay coincidencia, cuando busco, entonces se indica que puede registrarse un paciente nuevo.
```

## HU-03 — Consultar disponibilidad
**Historia:** Como recepcionista quiero consultar horarios por médico, especialidad y sucursal para ofrecer opciones reales.

```gherkin
Feature: Consultar disponibilidad
  Scenario: Camino válido
    Dado un rango y filtros válidos, cuando consulto, entonces solo aparecen espacios disponibles.

  Scenario: Validación o excepción
    Dado un filtro incompleto, cuando consulto, entonces se solicita completar los datos.
```

## HU-04 — Agendar cita
**Historia:** Como recepcionista quiero crear una cita con paciente, médico, sucursal, horario, especialidad y motivo.

```gherkin
Feature: Agendar cita
  Scenario: Camino válido
    Dada una solicitud válida y sin traslape, cuando guardo, entonces se crea en estado SOLICITADA.

  Scenario: Validación o excepción
    Dado un intervalo inválido, cuando guardo, entonces se rechaza con explicación.
```

## HU-05 — Prevenir duplicidad
**Historia:** Como recepcionista quiero impedir solapamientos de agenda para reducir citas duplicadas.

```gherkin
Feature: Prevenir duplicidad
  Scenario: Camino válido
    Dado que el médico tiene una cita activa en el intervalo, cuando intento agendar otra, entonces se rechaza por conflicto.

  Scenario: Validación o excepción
    Dado un intervalo libre, cuando agendo, entonces se permite continuar las validaciones.
```

## HU-06 — Confirmar cita
**Historia:** Como recepcionista quiero registrar la confirmación para saber si se contactó al paciente.

```gherkin
Feature: Confirmar cita
  Scenario: Camino válido
    Dada una cita solicitada, cuando confirmo, entonces pasa a CONFIRMADA y queda el evento.

  Scenario: Validación o excepción
    Dada una cita en estado no confirmable, cuando confirmo, entonces se rechaza la transición.
```

## HU-07 — Registrar llegada
**Historia:** Como recepcionista quiero registrar la hora real de llegada para medir tardanza.

```gherkin
Feature: Registrar llegada
  Scenario: Camino válido
    Dada una cita solicitada o confirmada, cuando registro llegada, entonces se guarda la hora y los minutos de retraso.

  Scenario: Validación o excepción
    Dado que la llegada ya fue registrada o la cita está cancelada, cuando repito la acción, entonces se rechaza.
```

## HU-08 — Decisión de tardanza
**Historia:** Como médico asignado quiero decidir si atiendo, espero o reprogramo a un paciente tardío.

```gherkin
Feature: Decisión de tardanza
  Scenario: Camino válido
    Dada una llegada registrada, cuando el médico asignado decide ATENDER, ESPERAR o REPROGRAMAR, entonces la decisión queda registrada.

  Scenario: Validación o excepción
    Dada una tardanza de 15 minutos o menos, el sistema no decide automáticamente y espera la decisión médica.
```

## HU-09 — Respetar autoridad médica
**Historia:** Como clínica quiero que solo el médico asignado decida la atención de una llegada para mantener responsabilidad clínica.

```gherkin
Feature: Respetar autoridad médica
  Scenario: Camino válido
    Dada una cita con llegada, cuando decide el médico asignado, entonces se acepta la decisión.

  Scenario: Validación o excepción
    Dada la solicitud de otro médico, cuando decide, entonces se rechaza por falta de autorización.
```

## HU-10 — Cancelar cita
**Historia:** Como recepcionista quiero cancelar una cita con motivo para conservar evidencia del cambio.

```gherkin
Feature: Cancelar cita
  Scenario: Camino válido
    Dada una cita cancelable, cuando la cancelo con motivo, entonces queda CANCELADA y se genera evento.

  Scenario: Validación o excepción
    Dada una cita ya atendida o cancelada, cuando intento cancelarla, entonces se rechaza.
```

## HU-11 — Registrar no-show
**Historia:** Como recepcionista quiero marcar una cita confirmada sin asistencia para medir inasistencias.

```gherkin
Feature: Registrar no-show
  Scenario: Camino válido
    Dada una cita solicitada o confirmada, cuando marco no-show, entonces queda NO_SHOW.

  Scenario: Validación o excepción
    Dada una cita atendida o cancelada, cuando marco no-show, entonces se rechaza.
```

## HU-12 — Autorizar aseguradora
**Historia:** Como recepcionista quiero registrar si la autorización previa requerida está concedida para no avanzar indebidamente.

```gherkin
Feature: Autorizar aseguradora
  Scenario: Camino válido
    Dado que el servicio requiere autorización y esta fue concedida, cuando valido la cita, entonces la especificación de autorización se cumple.

  Scenario: Validación o excepción
    Dado que se requiere autorización y no existe, cuando valido, entonces se rechaza el agendamiento.
```

## HU-13 — Registrar resultado de consulta
**Historia:** Como médico quiero registrar resultado, receta, estudios o seguimiento para conservar continuidad de atención.

```gherkin
Feature: Registrar resultado de consulta
  Scenario: Camino válido
    Dada una cita atendida, cuando registro resultado, entonces se asocia a la cita y paciente.

  Scenario: Validación o excepción
    Dada una cita no atendida, cuando intento registrar resultado clínico, entonces se solicita completar la atención primero.
```

## HU-14 — Consultar historial
**Historia:** Como personal autorizado quiero consultar el historial de citas de un paciente para conocer sus eventos previos.

```gherkin
Feature: Consultar historial
  Scenario: Camino válido
    Dado un identificador de paciente, cuando consulto historial, entonces se muestran citas y estados ordenados por fecha.

  Scenario: Validación o excepción
    Dado un identificador inexistente, cuando consulto, entonces se informa que no hay registro.
```

## HU-15 — Consultar indicadores
**Historia:** Como administración quiero consultar citas, cancelaciones, no-shows, duplicados y disponibilidad para analizar operación.

```gherkin
Feature: Consultar indicadores
  Scenario: Camino válido
    Dado un periodo válido, cuando solicito indicadores, entonces el sistema entrega conteos por estado y sucursal.

  Scenario: Validación o excepción
    Dado un periodo inválido, cuando solicito indicadores, entonces se devuelve error de validación.
```

## HU-16 — Gestionar agenda médica
**Historia:** Como médico quiero actualizar horarios semanales para que recepción use disponibilidad vigente.

```gherkin
Feature: Gestionar agenda médica
  Scenario: Camino válido
    Dado un horario válido, cuando se publica, entonces queda disponible para búsquedas futuras.

  Scenario: Validación o excepción
    Dado un horario que se solapa con uno existente, cuando se publica, entonces se advierte el conflicto.
```

## HU-17 — Reprogramar cita
**Historia:** Como recepcionista quiero proponer un nuevo horario cuando no pueda atenderse la cita original.

```gherkin
Feature: Reprogramar cita
  Scenario: Camino válido
    Dada una cita con estado REQUIERE_REPROGRAMACION, cuando se crea una nueva cita libre, entonces se conserva la relación de seguimiento.

  Scenario: Validación o excepción
    Dado que el nuevo horario no está disponible, cuando reprogramo, entonces no se confirma la nueva cita.
```

## HU-18 — Revisar sobrecupo
**Historia:** Como recepción quiero respetar la política individual del médico para no exceder su capacidad autorizada.

```gherkin
Feature: Revisar sobrecupo
  Scenario: Camino válido
    Dado un médico que permite sobrecupo y existe capacidad definida, cuando se solicita, entonces se valida el límite.

  Scenario: Validación o excepción
    Dado un médico que no permite sobrecupo, cuando se solicita uno, entonces se rechaza.
```

## HU-19 — Gestionar autorización
**Historia:** Como recepción quiero registrar la respuesta de aseguradora para que la atención sea trazable.

```gherkin
Feature: Gestionar autorización
  Scenario: Camino válido
    Dada una respuesta de autorización, cuando la registro, entonces queda asociada a la cita/servicio.

  Scenario: Validación o excepción
    Dado que el procedimiento no requiere autorización, cuando valido, entonces no se exige una respuesta.
```

## HU-20 — Auditar cambios
**Historia:** Como administración quiero conservar eventos de confirmación, llegada, decisión y cancelación para investigar cambios.

```gherkin
Feature: Auditar cambios
  Scenario: Camino válido
    Dado un cambio de dominio, cuando se aplica, entonces se genera un evento con tipo, cita y momento.

  Scenario: Validación o excepción
    Dada una transición inválida, cuando se intenta, entonces no se genera evento de éxito.
```
