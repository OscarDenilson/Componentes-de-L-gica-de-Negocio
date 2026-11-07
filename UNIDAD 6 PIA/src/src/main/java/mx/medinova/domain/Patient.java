package mx.medinova.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String city;

    private boolean active = true;

    protected Patient() {}

    public Patient(Long id, String name, String phone, String city, boolean active) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID de paciente inválido");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nombre obligatorio");
        this.id = id;
        this.name = name;
        this.phone = phone == null ? "" : phone;
        this.city = city == null ? "" : city;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCity() { return city; }
    public boolean isActive() { return active; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCity(String city) { this.city = city; }
    public void setActive(boolean active) { this.active = active; }
}

