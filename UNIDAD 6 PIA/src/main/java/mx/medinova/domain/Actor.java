package mx.medinova.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "actores")
public class Actor {
    @Id
    private String actorId;
    private String nombre;
    private boolean esPreferente;
    private boolean activo;
    private String ciudad;

    protected Actor() {}

    public Actor(String actorId, String nombre, boolean esPreferente, boolean activo, String ciudad) {
        if (actorId == null || actorId.isBlank()) throw new IllegalArgumentException("actorId obligatorio");
        this.actorId = actorId;
        this.nombre = nombre;
        this.esPreferente = esPreferente;
        this.activo = activo;
        this.ciudad = ciudad;
    }

    public String getActorId() { return actorId; }
    public String getNombre() { return nombre; }
    public boolean isEsPreferente() { return esPreferente; }
    public boolean isActivo() { return activo; }
    public String getCiudad() { return ciudad; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEsPreferente(boolean esPreferente) { this.esPreferente = esPreferente; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
}

