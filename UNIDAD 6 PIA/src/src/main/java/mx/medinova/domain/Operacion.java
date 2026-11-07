package mx.medinova.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operaciones")
public class Operacion {
    @Id
    private String operacionId;

    @Column(nullable = false)
    private String actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperacionStatus estado = OperacionStatus.SOLICITADA;

    private LocalDate fechaSolicitud;
    private boolean promocionActiva;
    private boolean autorizado;
    private String referencia;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalMxn = BigDecimal.ZERO;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "operacionId")
    private List<DetalleOperacion> lineas = new ArrayList<>();

    @Transient
    private final List<DomainEvent> events = new ArrayList<>();

    protected Operacion() {}

    public Operacion(String operacionId, String actorId, LocalDate fechaSolicitud, boolean promocionActiva, boolean autorizado, String referencia) {
        if (operacionId == null || operacionId.isBlank()) throw new IllegalArgumentException("operacionId obligatorio");
        if (actorId == null || actorId.isBlank()) throw new IllegalArgumentException("actorId obligatorio");
        this.operacionId = operacionId;
        this.actorId = actorId;
        this.fechaSolicitud = fechaSolicitud == null ? LocalDate.now() : fechaSolicitud;
        this.promocionActiva = promocionActiva;
        this.autorizado = autorizado;
        this.referencia = referencia == null ? "" : referencia;
        this.estado = OperacionStatus.SOLICITADA;
    }

    public void agregarLinea(DetalleOperacion detalle) {
        if (detalle != null) {
            this.lineas.add(detalle);
            recalcularTotal();
        }
    }

    public void recalcularTotal() {
        this.totalMxn = this.lineas.stream()
            .map(DetalleOperacion::getSubtotalMxn)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void aprobar() {
        if (this.estado != OperacionStatus.SOLICITADA) {
            throw new IllegalStateException("Transición inválida: No se puede aprobar desde " + this.estado);
        }
        this.estado = OperacionStatus.CONFIRMADA;
        emit("OperacionConfirmada", "Operación " + operacionId + " aprobada exitosamente");
    }

    public void completar() {
        if (this.estado != OperacionStatus.CONFIRMADA) {
            throw new IllegalStateException("Transición inválida: No se puede completar desde " + this.estado + ". Requiere estar CONFIRMADA.");
        }
        this.estado = OperacionStatus.ATENDIDA;
        emit("OperacionAtendida", "Operación " + operacionId + " atendida/completada exitosamente");
    }

    public void cancelar(String motivo) {
        if (this.estado == OperacionStatus.ATENDIDA || this.estado == OperacionStatus.CANCELADA) {
            throw new IllegalStateException("Transición inválida: No se puede cancelar en estado " + this.estado);
        }
        this.estado = OperacionStatus.CANCELADA;
        emit("OperacionCancelada", motivo == null ? "Cancelada por usuario" : motivo);
    }

    private void emit(String tipo, String detalle) {
        this.events.add(new DomainEvent(tipo, null, Instant.now(), detalle));
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String getOperacionId() { return operacionId; }
    public String getActorId() { return actorId; }
    public OperacionStatus getEstado() { return estado; }
    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public boolean isPromocionActiva() { return promocionActiva; }
    public boolean isAutorizado() { return autorizado; }
    public String getReferencia() { return referencia; }
    public BigDecimal getTotalMxn() { return totalMxn; }
    public List<DetalleOperacion> getLineas() { return lineas; }

    public void setEstado(OperacionStatus estado) { this.estado = estado; }
    public void setTotalMxn(BigDecimal totalMxn) { this.totalMxn = totalMxn; }
    public void setAutorizado(boolean autorizado) { this.autorizado = autorizado; }
}
