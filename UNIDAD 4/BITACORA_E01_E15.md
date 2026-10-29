# Bitácora de escenarios E01–E15

Cada escenario se ejecuta con fixtures recién cargados para aislar los cambios de estado. Los importes son MXN. “Evento” lista únicamente los eventos emitidos por operaciones válidas; las operaciones rechazadas no generan eventos.

| Caso | Precondiciones / estado inicial | Operación | Resultado esperado y real | Evento | Prueba |
|---|---|---|---|---|---|
| E01 | O001, DRAFT, cliente VIP activo, 4 unidades, total $2,100, sin promoción | Evaluar descuento | 10%; descuento $210; total neto $1,890 | Ninguno; evaluar no cambia estado | `test_o001_descuento_vip_10_y_total` |
| E02 | O002, DRAFT, cliente no VIP, 2 unidades, total $1,050, sin promoción | Evaluar descuento | 0%; no cumple VIP ni total > $5,000 | Ninguno | Prueba de descuento de O002 / verificación de fixture |
| E03 | O003, DRAFT, cliente VIP pero inactivo, 4 unidades | `confirm()` | Rechazo `InvalidOrderData`; permanece DRAFT | Ninguno | `test_cliente_inactivo_no_confirma_sin_evento` |
| E04 | O005, DRAFT, promoción activa, total $2,100 | Evaluar descuento | 0%; la promoción excluye ambas reglas | Ninguno | `test_promocion_excluye_descuento` |
| E05 | O006, DRAFT, total $18,000, sin promoción | Evaluar descuento | 5%; descuento $900; neto $17,100 | Ninguno | `test_o006_descuento_5` |
| E06 | O007, DRAFT, cliente activo, sin artículos | `confirm()` | Rechazo `InvalidOrderData`; permanece DRAFT | Ninguno | `test_pedido_vacio_no_confirma` |
| E07 | O008, DRAFT, 20 unidades exactas | `confirm()` | Éxito; pasa a CONFIRMED | `OrderConfirmed` | `test_20_unidades_se_permiten` |
| E08 | O009, DRAFT, 19 unidades | Agregar SKU003 × 2 | Rechazo `OrderItemLimitExceeded`; no agrega las 2 unidades, conserva 19 | Ninguno | `test_limite_suma_unidades_no_lineas` |
| E09 | O010, DRAFT, total exacto $500 (SKU003 + SKU009) | Evaluar descuento | 0%; el umbral usa `>` y no `>=` | Ninguno | `test_umbral_estricto_500` |
| E10 | O011, DRAFT, cliente VIP, total $1,500, 3 unidades | Evaluar descuento | 0%; no alcanza las 4 unidades mínimas y no supera $5,000 | Ninguno | `test_specification_and_or_not` y comprobación del fixture |
| E11 | O012, DRAFT, pedido con artículo y cliente activo | `confirm()` y luego `ship("")` | Confirmación válida; envío rechazado por guía vacía. Estado final CONFIRMED | Solo `OrderConfirmed`; no se emite `OrderShipped` | `test_envio_requiere_guia_y_no_mutacion` |
| E12 | O013, DRAFT, pedido válido | Confirmar dos veces | Primera confirmación válida; segunda rechazada con `InvalidOrderTransition`; estado CONFIRMED | Un solo `OrderConfirmed` | `test_confirmacion_doble_no_duplica_evento` |
| E13 | O014, DRAFT, pedido válido | Confirmar, enviar con guía y cancelar | Confirmar y enviar válidos; cancelar rechazado desde SHIPPED; estado final SHIPPED | `OrderConfirmed`, `OrderShipped` | `test_no_cancelar_enviado` |
| E14 | O015, DRAFT, pedido válido | Confirmar, enviar con guía y devolver | Éxito; estado final RETURNED | `OrderConfirmed`, `OrderShipped`, `OrderReturned` | `test_devolver_y_entregar` |
| E15 | O016, DRAFT, pedido válido | Confirmar, enviar con guía y entregar | Éxito; estado final DELIVERED | `OrderConfirmed`, `OrderShipped`, `OrderDelivered` | `test_devolver_y_entregar` |

## Observaciones de implementación

- Las reglas se validan en el dominio; FastAPI traduce errores a códigos HTTP.
- Las pruebas usan una carga limpia de CSV para evitar que un escenario contamine al siguiente.
- E11 demuestra que una operación inválida posterior no revierte una transición previa válida, pero tampoco agrega un evento de envío.
- No se descuenta existencia de productos: el stock es únicamente información de catálogo.
