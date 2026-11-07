package mx.medinova.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name="appointments", indexes=@Index(name="idx_appointment_start", columnList="doctor_id,start_at"))
public class Appointment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long patientId;

    @Column(nullable=false)
    private Long doctorId;

    @Column(nullable=false)
    private Long branchId;

    @Column(nullable=false)
    private String specialty;

    @Column(nullable=false)
    private LocalDateTime startAt;

    @Column(nullable=false)
    private LocalDateTime endAt;

    @Column(nullable=false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private AppointmentStatus status = AppointmentStatus.SOLICITADA;

    private LocalDateTime arrivedAt;
    private Integer latenessMinutes;

    @Enumerated(EnumType.STRING)
    private MedicalDecision medicalDecision;

    private String decisionNotes;
    private LocalDateTime decisionAt;

    private String operacionId;

    @Transient
    private final List<DomainEvent> events = new ArrayList<>();

    protected Appointment() {}

    public Appointment(Long patientId, Long doctorId, Long branchId, String specialty, LocalDateTime startAt, LocalDateTime endAt, String reason) {
        this(patientId, doctorId, branchId, specialty, startAt, endAt, reason, null);
    }

    public Appointment(Long patientId, Long doctorId, Long branchId, String specialty, LocalDateTime startAt, LocalDateTime endAt, String reason, String operacionId) {
        if (patientId == null || doctorId == null || branchId == null) throw new IllegalArgumentException("Paciente, médico y sucursal son obligatorios");
        if (specialty == null || specialty.isBlank() || reason == null || reason.isBlank()) throw new IllegalArgumentException("Especialidad y motivo son obligatorios");
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) throw new IllegalArgumentException("El fin debe ser posterior al inicio");
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.branchId = branchId;
        this.specialty = specialty;
        this.startAt = startAt;
        this.endAt = endAt;
        this.reason = reason;
        this.operacionId = operacionId;
    }

    public void confirm() {
        requireStatus(AppointmentStatus.SOLICITADA);
        status = AppointmentStatus.CONFIRMADA;
        emit("CitaConfirmada", "confirmada");
    }

    public void registerArrival(LocalDateTime time) {
        if (status != AppointmentStatus.SOLICITADA && status != AppointmentStatus.CONFIRMADA) {
            throw new IllegalStateException("No se puede registrar llegada desde " + status);
        }
        if (time == null) throw new IllegalArgumentException("Hora de llegada obligatoria");
        arrivedAt = time;
        latenessMinutes = (int) Math.max(0, Duration.between(startAt, time).toMinutes());
        status = AppointmentStatus.LLEGADA_REGISTRADA;
        emit("LlegadaRegistrada", "minutosTarde=" + latenessMinutes);
    }

    public void decide(Long actingDoctorId, MedicalDecision decision, String notes, LocalDateTime now) {
        if (status != AppointmentStatus.LLEGADA_REGISTRADA) {
            throw new IllegalStateException("Primero debe registrarse la llegada");
        }
        if (!Objects.equals(doctorId, actingDoctorId)) {
            throw new SecurityException("Solo el médico asignado puede decidir");
        }
        if (decision == null) throw new IllegalArgumentException("Decisión obligatoria");
        medicalDecision = decision;
        decisionNotes = notes;
        decisionAt = now == null ? LocalDateTime.now() : now;
        switch (decision) {
            case ATENDER -> status = AppointmentStatus.ATENDIDA;
            case ESPERAR -> status = AppointmentStatus.LLEGADA_REGISTRADA;
            case REPROGRAMAR -> status = AppointmentStatus.REQUIERE_REPROGRAMACION;
        }
        emit("DecisionMedicaRegistrada", decision.name());
    }

    public void cancel(String reason) {
        if (status == AppointmentStatus.ATENDIDA || status == AppointmentStatus.CANCELADA || status == AppointmentStatus.NO_SHOW) {
            throw new IllegalStateException("Estado no cancelable: " + status);
        }
        status = AppointmentStatus.CANCELADA;
        decisionNotes = reason;
        emit("CitaCancelada", reason == null ? "" : reason);
    }

    public void markNoShow() {
        if (status != AppointmentStatus.CONFIRMADA && status != AppointmentStatus.SOLICITADA) {
            throw new IllegalStateException("No-show inválido desde " + status);
        }
        status = AppointmentStatus.NO_SHOW;
        emit("NoShowRegistrado", "no-show");
    }

    private void requireStatus(AppointmentStatus s) {
        if (status != s) throw new IllegalStateException("Transición inválida desde " + status);
    }

    private void emit(String type, String detail) {
        events.add(new DomainEvent(type, id, Instant.now(), detail));
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
    public Long getBranchId() { return branchId; }
    public String getSpecialty() { return specialty; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public String getReason() { return reason; }
    public AppointmentStatus getStatus() { return status; }
    public LocalDateTime getArrivedAt() { return arrivedAt; }
    public Integer getLatenessMinutes() { return latenessMinutes; }
    public MedicalDecision getMedicalDecision() { return medicalDecision; }
    public String getDecisionNotes() { return decisionNotes; }
    public LocalDateTime getDecisionAt() { return decisionAt; }
    public String getOperacionId() { return operacionId; }
    public void setOperacionId(String operacionId) { this.operacionId = operacionId; }
}
