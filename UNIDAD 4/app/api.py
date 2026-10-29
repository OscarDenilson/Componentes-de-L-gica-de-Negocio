from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from cargar_csv import load_fixture
from app.domain import InvalidOrderData, InvalidOrderTransition, OrderStatus

app = FastAPI(title="TechStore U4", version="1.0.0", description="Núcleo de negocio en memoria; los cambios se reinician al reiniciar el proceso.")
orders = load_fixture()

class ShipRequest(BaseModel):
    tracking_number: str
class CancelRequest(BaseModel):
    reason: str

def get_order(order_id):
    order = orders.get(order_id)
    if order is None: raise HTTPException(status_code=404, detail="No existe el pedido")
    return order
def response(order):
    return {"id":order.id,"customer_id":order.customer.id,"status":order.status.value,
            "units":order.units,"total_mxn":str(order.total.amount),
            "discount_rate":str(order.discount_rate()),"discount_mxn":str(order.discount_amount.amount),
            "payable_mxn":str(order.payable_total.amount),"events":[e.name for e in order.events]}
def run_action(order, action):
    try: action()
    except InvalidOrderTransition as exc: raise HTTPException(status_code=409, detail=str(exc))
    except InvalidOrderData as exc: raise HTTPException(status_code=422, detail=str(exc))
    return response(order)

@app.get("/health")
def health(): return {"status":"ok"}
@app.get("/orders")
def list_orders(): return [response(o) for o in orders.values()]
@app.get("/orders/{order_id}")
def get_order_endpoint(order_id: str): return response(get_order(order_id))
@app.post("/orders/{order_id}/confirm")
def confirm_order(order_id: str):
    order=get_order(order_id); return run_action(order, order.confirm)
@app.post("/orders/{order_id}/ship")
def ship_order(order_id: str, body: ShipRequest):
    order=get_order(order_id); return run_action(order, lambda: order.ship(body.tracking_number))
@app.post("/orders/{order_id}/cancel")
def cancel_order(order_id: str, body: CancelRequest):
    order=get_order(order_id); return run_action(order, lambda: order.cancel(body.reason))
@app.post("/orders/{order_id}/deliver")
def deliver_order(order_id: str):
    order=get_order(order_id); return run_action(order, order.deliver)
@app.post("/orders/{order_id}/return")
def return_order(order_id: str):
    order=get_order(order_id); return run_action(order, order.return_order)
