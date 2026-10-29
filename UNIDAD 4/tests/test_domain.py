from decimal import Decimal
import pytest
from app.domain import Money, Customer, Order, OrderItem, InvalidOrderData, InvalidOrderTransition, OrderItemLimitExceeded, OrderStatus
from app.specifications import MinimumAmountSpec, VIPCustomerSpec, MinimumUnitsSpec, ActivePromotionSpec
from cargar_csv import load_fixture

def test_money_rechaza_negativo_y_float_preciso():
    with pytest.raises(InvalidOrderData): Money(Decimal("-0.01"))
    assert Money("0.1").add(Money("0.2")).amount == Decimal("0.3")

def test_o001_descuento_vip_10_y_total():
    o=load_fixture()["O001"]
    assert (o.units, o.total.amount, o.discount_rate(), o.payable_total.amount) == (4, Decimal("2100.00"), Decimal("0.10"), Decimal("1890.0000"))

def test_o006_descuento_5():
    o=load_fixture()["O006"]
    assert o.discount_rate() == Decimal("0.05")

def test_promocion_excluye_descuento():
    assert load_fixture()["O005"].discount_rate() == Decimal("0")

def test_umbral_estricto_500():
    assert load_fixture()["O010"].total.amount == Decimal("500.00")
    assert load_fixture()["O010"].discount_rate() == Decimal("0")

def test_limite_suma_unidades_no_lineas():
    o=load_fixture()["O009"]
    with pytest.raises(OrderItemLimitExceeded): o.add_item(OrderItem("SKU003",2,Money("120")))
    assert o.units == 19

def test_cliente_inactivo_no_confirma_sin_evento():
    o=load_fixture()["O003"]
    with pytest.raises(InvalidOrderData): o.confirm()
    assert o.status == OrderStatus.DRAFT and not o.events

def test_pedido_vacio_no_confirma():
    o=load_fixture()["O007"]
    with pytest.raises(InvalidOrderData): o.confirm()
    assert not o.events

def test_20_unidades_se_permiten():
    o=load_fixture()["O008"]; o.confirm()
    assert o.status == OrderStatus.CONFIRMED and [e.name for e in o.events] == ["OrderConfirmed"]

def test_confirmacion_doble_no_duplica_evento():
    o=load_fixture()["O013"]; o.confirm()
    with pytest.raises(InvalidOrderTransition): o.confirm()
    assert len(o.events)==1

def test_envio_requiere_guia_y_no_mutacion():
    o=load_fixture()["O012"]; o.confirm()
    with pytest.raises(InvalidOrderData): o.ship("")
    assert o.status == OrderStatus.CONFIRMED and [e.name for e in o.events]==["OrderConfirmed"]

def test_no_cancelar_enviado():
    o=load_fixture()["O014"]; o.confirm(); o.ship("TRK-1")
    with pytest.raises(InvalidOrderTransition): o.cancel("Cambio")
    assert o.status == OrderStatus.SHIPPED and [e.name for e in o.events]==["OrderConfirmed","OrderShipped"]

def test_devolver_y_entregar():
    a=load_fixture()["O015"]; a.confirm(); a.ship("TRK-15"); a.return_order()
    assert a.status == OrderStatus.RETURNED and a.events[-1].name=="OrderReturned"
    b=load_fixture()["O016"]; b.confirm(); b.ship("TRK-16"); b.deliver()
    assert b.status == OrderStatus.DELIVERED and b.events[-1].name=="OrderDelivered"

def test_specification_and_or_not():
    o=load_fixture()["O001"]
    assert (MinimumAmountSpec(500) & VIPCustomerSpec() & MinimumUnitsSpec(4) & ~ActivePromotionSpec()).is_satisfied_by(o)
    assert (VIPCustomerSpec() | MinimumAmountSpec(5000)).is_satisfied_by(o)

def test_api_404_409_422():
    from fastapi.testclient import TestClient
    from app.api import app, orders
    client=TestClient(app)
    assert client.get("/orders/NO-EXISTE").status_code==404
    orders.clear(); orders.update(load_fixture())
    assert client.post("/orders/O013/confirm").status_code==200
    assert client.post("/orders/O013/confirm").status_code==409
    assert client.post("/orders/O007/confirm").status_code==422

def test_e02_e10_sin_descuento_por_condiciones():
    orders=load_fixture()
    assert orders["O002"].discount_rate()==Decimal("0")
    assert orders["O011"].units==3 and orders["O011"].discount_rate()==Decimal("0")

def test_e11_confirmacion_valida_envio_invalido_conserva_evento():
    o=load_fixture()["O012"]
    o.confirm()
    with pytest.raises(InvalidOrderData): o.ship("")
    assert o.status==OrderStatus.CONFIRMED
    assert [e.name for e in o.events]==["OrderConfirmed"]

def test_e13_e14_e15_transiciones_y_eventos():
    orders=load_fixture()
    o=orders["O014"]; o.confirm(); o.ship("TRK-14")
    with pytest.raises(InvalidOrderTransition): o.cancel("Ya enviado")
    assert o.status==OrderStatus.SHIPPED
    assert [e.name for e in o.events]==["OrderConfirmed","OrderShipped"]
    r=orders["O015"]; r.confirm(); r.ship("TRK-15"); r.return_order()
    assert r.status==OrderStatus.RETURNED
    d=orders["O016"]; d.confirm(); d.ship("TRK-16"); d.deliver()
    assert d.status==OrderStatus.DELIVERED
