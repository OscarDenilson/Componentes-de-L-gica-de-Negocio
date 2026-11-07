package mx.medinova.application;

import mx.medinova.domain.*;
import mx.medinova.infrastructure.AppointmentRepository;
import mx.medinova.infrastructure.ActorRepository;
import mx.medinova.infrastructure.DoctorRepository;
import mx.medinova.infrastructure.OperacionRepository;
import mx.medinova.infrastructure.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentService {
    private final AppointmentRepository repo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final OperacionRepository operacionRepo;
    private final ActorRepository actorRepo;

    public AppointmentService(
        AppointmentRepository repo,
        PatientRepository patientRepo,
        DoctorRepository doctorRepo,
        OperacionRepository operacionRepo,
        ActorRepository actorRepo
    ) {
        this.repo = repo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.operacionRepo = operacionRepo;
        this.actorRepo = actorRepo;
    }

    @Transactional
    public Appointment create(AppointmentSpecifications.BookingRequest r, String reason) {
        if (r.operacionId() != null && !r.operacionId().isBlank()) {
            Operacion op = operacionRepo.findById(r.operacionId())
                .orElseThrow(() -> new IllegalArgumentException("No es posible crear la cita con esa ID: La operación " + r.operacionId() + " no existe"));
            if (!op.isAutorizado()) {
                throw new IllegalArgumentException("No es posible crear la cita: La operación " + r.operacionId() + " para el paciente " + op.getActorId() + " no está autorizada. Requiere autorización médica previa.");
            }
        }

        boolean patientExists = patientRepo.existsById(r.patientId()) ||
            actorRepo.existsById("CITA-C" + String.format("%03d", r.patientId())) ||
            actorRepo.existsById("CITA-C" + r.patientId());
        if (!patientExists) {
            throw new IllegalArgumentException("No es posible crear la cita con esa ID: El paciente con ID " + r.patientId() + " no existe");
        }

        Doctor doctor = doctorRepo.findById(r.doctorId())
            .orElseThrow(() -> new IllegalArgumentException("No es posible crear la cita con esa ID: El médico con ID " + r.doctorId() + " no existe"));

        if (r.specialty() == null || r.specialty().isBlank() || !r.specialty().trim().equalsIgnoreCase(doctor.getSpecialty().trim())) {
            throw new IllegalArgumentException("No es posible crear la cita: La especialidad '" + r.specialty() + "' no existe o no corresponde al médico asignado (" + doctor.getSpecialty() + ")");
        }

        if (!AppointmentSpecifications.canBook().test(r)) {
            throw new IllegalArgumentException("No se cumplen las especificaciones para agendar");
        }
        boolean overlap = repo.existsByDoctorIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            r.doctorId(),
            List.of(AppointmentStatus.SOLICITADA, AppointmentStatus.CONFIRMADA, AppointmentStatus.LLEGADA_REGISTRADA),
            r.endAt(),
            r.startAt()
        );
        if (overlap) {
            throw new IllegalStateException("El médico ya tiene una cita en ese intervalo");
        }
        return repo.save(new Appointment(r.patientId(), r.doctorId(), r.branchId(), r.specialty(), r.startAt(), r.endAt(), reason, r.operacionId()));
    }

    public List<Appointment> all() {
        return repo.findAll();
    }

    public Appointment get(long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Cita no encontrada: " + id));
    }

    @Transactional
    public Appointment confirm(long id) {
        Appointment a = get(id);
        a.confirm();
        return repo.save(a);
    }

    @Transactional
    public Appointment arrival(long id, LocalDateTime time) {
        Appointment a = get(id);
        a.registerArrival(time);
        return repo.save(a);
    }

    @Transactional
    public Appointment decide(long id, Long doctorId, MedicalDecision d, String notes) {
        Appointment a = get(id);
        a.decide(doctorId, d, notes, LocalDateTime.now());
        return repo.save(a);
    }

    @Transactional
    public Appointment cancel(long id, String reason) {
        Appointment a = get(id);
        a.cancel(reason);
        return repo.save(a);
    }

    @Transactional
    public Appointment noShow(long id) {
        Appointment a = get(id);
        a.markNoShow();
        return repo.save(a);
    }
}
