package mx.medinova.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_operaciones")
public class DetalleOperacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String operacionId;

    @Column(nullable = false)
    private String recursoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitarioMxn;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalMxn;

    protected DetalleOperacion() {}

    public DetalleOperacion(String operacionId, String recursoId, Integer cantidad, BigDecimal precioUnitarioMxn) {
        if (operacionId == null || operacionId.isBlank()) throw new IllegalArgumentException("operacionId obligatorio");
        if (recursoId == null || recursoId.isBlank()) throw new IllegalArgumentException("recursoId obligatorio");
        if (cantidad == null || cantidad <= 0) throw new IllegalArgumentException("cantidad debe ser mayor a 0");
        this.operacionId = operacionId;
        this.recursoId = recursoId;
        this.cantidad = cantidad;
        this.precioUnitarioMxn = precioUnitarioMxn == null ? BigDecimal.ZERO : precioUnitarioMxn;
        this.subtotalMxn = this.precioUnitarioMxn.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() { return id; }
    public String getOperacionId() { return operacionId; }
    public String getRecursoId() { return recursoId; }
    public Integer getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitarioMxn() { return precioUnitarioMxn; }
    public BigDecimal getSubtotalMxn() { return subtotalMxn; }
}

