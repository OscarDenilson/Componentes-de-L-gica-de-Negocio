package mx.medinova.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialty;

    @Column(nullable = false)
    private Long branchId;

    private boolean active = true;

    protected Doctor() {}

    public Doctor(Long id, String name, String specialty, Long branchId, boolean active) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID de médico inválido");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nombre obligatorio");
        if (specialty == null || specialty.isBlank()) throw new IllegalArgumentException("Especialidad obligatoria");
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.branchId = branchId == null ? 1L : branchId;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public Long getBranchId() { return branchId; }
    public boolean isActive() { return active; }
    public void setName(String name) { this.name = name; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public void setActive(boolean active) { this.active = active; }
}

