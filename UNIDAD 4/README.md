# TechStore — Actividad 4: Implementación del núcleo de negocio

Proyecto de laboratorio con Python 3.11+, FastAPI y pytest. Implementa entidades/Value Objects, Specification Pattern, máquina de estados, Domain Events y API REST. Los datos se leen desde CSV y los cambios se conservan únicamente en memoria durante la ejecución.

## Instalación y ejecución

```bash
python -m venv .venv
# Windows PowerShell: .venv\Scripts\Activate.ps1
# Windows CMD: .venv\Scripts\activate.bat
# macOS/Linux: source .venv/bin/activate
python -m pip install -r requirements.txt
python cargar_csv.py
python -m pytest -q
uvicorn app.api:app --reload
```

Abre `http://127.0.0.1:8000/docs` para Swagger y `http://127.0.0.1:8000/openapi.json` para OpenAPI.

## Reglas de negocio

- `Money` usa `Decimal`, rechaza importes negativos/no finitos y no permite sumar monedas diferentes.
- Un pedido comienza `DRAFT`; únicamente `DRAFT` permite agregar artículos.
- Máximo 20 unidades en total por pedido (suma de cantidades, no líneas).
- Descuento del 10% si se cumplen simultáneamente: cliente VIP, total estrictamente mayor a $500 MXN, mínimo 4 unidades y sin promoción.
- Descuento del 5% si total estrictamente mayor a $5,000 MXN y sin promoción. Si ambas reglas aplican, tiene prioridad 10%.
- No hay control de inventario: existencia es dato de catálogo.
- Solo clientes activos y pedidos con artículos se pueden confirmar.
- Enviar exige pedido `CONFIRMED` y guía no vacía; cancelar exige `CONFIRMED` y motivo; entregar/devolver exige `SHIPPED`.
- Si una operación falla, no cambia el estado ni añade evento.

## API

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/health` | Estado del servicio |
| GET | `/orders` | Todos los pedidos |
| GET | `/orders/{id}` | Pedido, total, descuento, estado y eventos |
| POST | `/orders/{id}/confirm` | Confirma |
| POST | `/orders/{id}/ship` | JSON `{"tracking_number":"TRK-123"}` |
| POST | `/orders/{id}/cancel` | JSON `{"reason":"Motivo"}` |
| POST | `/orders/{id}/deliver` | Marca entregado |
| POST | `/orders/{id}/return` | Marca devuelto |

Códigos semánticos: `404` pedido inexistente, `409` transición inválida, `422` datos/reglas de dominio inválidos.

## Estructura

- `app/domain.py`: entidades, Value Objects, invariantes, estados y eventos.
- `app/specifications.py`: AND, OR, NOT y reglas de descuento.
- `app/api.py`: adaptador HTTP FastAPI.
- `cargar_csv.py`, `datos/`: fixtures entregados.
- `tests/`: pruebas unitarias y de API.
- `BITACORA_E01_E15.md`: resolución documentada de los 15 escenarios.
- `docs/estados.md`: diagrama de estados y eventos.

La API carga fixtures al iniciar. No hay base de datos y los cambios no persisten tras reiniciar.
