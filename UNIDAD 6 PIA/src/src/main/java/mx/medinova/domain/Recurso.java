package mx.medinova.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "recursos")
public class Recurso {
    @Id
    private String recursoId;
    private String descripcion;
    private BigDecimal precioMxn;
    private Integer capacidadOStock;
    private boolean habilitado;

    protected Recurso() {}

    public Recurso(String recursoId, String descripcion, BigDecimal precioMxn, Integer capacidadOStock, boolean habilitado) {
        if (recursoId == null || recursoId.isBlank()) throw new IllegalArgumentException("recursoId obligatorio");
        this.recursoId = recursoId;
        this.descripcion = descripcion;
        this.precioMxn = precioMxn == null ? BigDecimal.ZERO : precioMxn;
        this.capacidadOStock = capacidadOStock == null ? 0 : capacidadOStock;
        this.habilitado = habilitado;
    }

    public String getRecursoId() { return recursoId; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecioMxn() { return precioMxn; }
    public Integer getCapacidadOStock() { return capacidadOStock; }
    public boolean isHabilitado() { return habilitado; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecioMxn(BigDecimal precioMxn) { this.precioMxn = precioMxn; }
    public void setCapacidadOStock(Integer capacidadOStock) { this.capacidadOStock = capacidadOStock; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}

