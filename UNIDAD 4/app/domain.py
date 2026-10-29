from dataclasses import dataclass, field
from datetime import datetime, timezone
from decimal import Decimal
from enum import Enum

class InvalidOrderData(ValueError): pass
class InvalidOrderTransition(RuntimeError): pass
class OrderItemLimitExceeded(InvalidOrderData): pass

@dataclass(frozen=True)
class Money:
    amount: Decimal
    currency: str = "MXN"
    def __post_init__(self):
        try: amount = self.amount if isinstance(self.amount, Decimal) else Decimal(str(self.amount))
        except Exception as exc: raise InvalidOrderData("Importe inválido") from exc
        if not amount.is_finite() or amount < 0: raise InvalidOrderData("Money no admite importes negativos ni no finitos")
        object.__setattr__(self, "amount", amount)
        if not self.currency or not self.currency.strip(): raise InvalidOrderData("Moneda requerida")
    def add(self, other):
        if self.currency != other.currency: raise InvalidOrderData("No se pueden sumar monedas distintas")
        return Money(self.amount + other.amount, self.currency)
    def multiply(self, factor): return Money(self.amount * Decimal(str(factor)), self.currency)

@dataclass(frozen=True)
class Customer:
    id: str
    is_vip: bool
    active: bool
    def __post_init__(self):
        if not self.id or not self.id.strip(): raise InvalidOrderData("ID de cliente requerido")

@dataclass(frozen=True)
class OrderItem:
    sku: str
    quantity: int
    unit_price: Money
    def __post_init__(self):
        if not self.sku or not self.sku.strip(): raise InvalidOrderData("SKU requerido")
        if isinstance(self.quantity, bool) or not isinstance(self.quantity, int) or self.quantity <= 0:
            raise InvalidOrderData("La cantidad debe ser un entero positivo")
        if not isinstance(self.unit_price, Money): raise InvalidOrderData("Precio debe ser Money")
    @property
    def subtotal(self): return self.unit_price.multiply(self.quantity)

class OrderStatus(str, Enum):
    DRAFT="DRAFT"; CONFIRMED="CONFIRMED"; SHIPPED="SHIPPED"
    DELIVERED="DELIVERED"; CANCELLED="CANCELLED"; RETURNED="RETURNED"

@dataclass(frozen=True)
class DomainEvent:
    name: str
    order_id: str
    occurred_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    details: str = ""

@dataclass
class Order:
    customer: Customer
    id: str
    has_promotion: bool = False
    status: OrderStatus = OrderStatus.DRAFT
    items: list[OrderItem] = field(default_factory=list)
    events: list[DomainEvent] = field(default_factory=list)
    tracking_number: str | None = None
    @property
    def units(self): return sum(item.quantity for item in self.items)
    @property
    def total(self):
        amount = sum((item.subtotal.amount for item in self.items), Decimal("0.00"))
        return Money(amount)
    def add_item(self, item):
        if self.status != OrderStatus.DRAFT: raise InvalidOrderTransition("Solo DRAFT admite artículos")
        if self.units + item.quantity > 20: raise OrderItemLimitExceeded("Máximo 20 unidades por pedido")
        if item.unit_price.currency != "MXN": raise InvalidOrderData("Solo se admite MXN")
        self.items.append(item)
    def discount_rate(self):
        from app.specifications import MinimumAmountSpec, VIPCustomerSpec, MinimumUnitsSpec, ActivePromotionSpec
        ten = MinimumAmountSpec(Decimal("500")) & VIPCustomerSpec() & MinimumUnitsSpec(4) & ~ActivePromotionSpec()
        five = MinimumAmountSpec(Decimal("5000")) & ~ActivePromotionSpec()
        if ten.is_satisfied_by(self): return Decimal("0.10")
        if five.is_satisfied_by(self): return Decimal("0.05")
        return Decimal("0")
    @property
    def discount_amount(self): return Money((self.total.amount * self.discount_rate()).quantize(Decimal("0.01")))
    @property
    def payable_total(self): return Money((self.total.amount - self.discount_amount.amount).quantize(Decimal("0.01")))
    def _event(self, name, details=""): self.events.append(DomainEvent(name, self.id, details=details))
    def confirm(self):
        if self.status != OrderStatus.DRAFT: raise InvalidOrderTransition("Solo se confirma desde DRAFT")
        if not self.items: raise InvalidOrderData("No se puede confirmar un pedido vacío")
        if not self.customer.active: raise InvalidOrderData("El cliente está inactivo")
        self.status=OrderStatus.CONFIRMED; self._event("OrderConfirmed")
    def ship(self, tracking_number):
        if self.status != OrderStatus.CONFIRMED: raise InvalidOrderTransition("Solo se envía desde CONFIRMED")
        if not isinstance(tracking_number, str) or not tracking_number.strip(): raise InvalidOrderData("La guía de envío es obligatoria")
        self.tracking_number=tracking_number.strip(); self.status=OrderStatus.SHIPPED; self._event("OrderShipped", self.tracking_number)
    def cancel(self, reason):
        if self.status != OrderStatus.CONFIRMED: raise InvalidOrderTransition("Solo se cancela desde CONFIRMED")
        if not isinstance(reason, str) or not reason.strip(): raise InvalidOrderData("El motivo de cancelación es obligatorio")
        self.status=OrderStatus.CANCELLED; self._event("OrderCancelled", reason.strip())
    def deliver(self):
        if self.status != OrderStatus.SHIPPED: raise InvalidOrderTransition("Solo se entrega desde SHIPPED")
        self.status=OrderStatus.DELIVERED; self._event("OrderDelivered")
    def return_order(self):
        if self.status != OrderStatus.SHIPPED: raise InvalidOrderTransition("Solo se devuelve desde SHIPPED")
        self.status=OrderStatus.RETURNED; self._event("OrderReturned")
