package mx.medinova.api;

import mx.medinova.domain.Patient;
import mx.medinova.infrastructure.AppointmentRepository;
import mx.medinova.infrastructure.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientRepository patientRepo;
    private final AppointmentRepository appointmentRepo;

    public PatientController(PatientRepository patientRepo, AppointmentRepository appointmentRepo) {
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
    }

    @GetMapping
    public List<Patient> all() {
        return patientRepo.findAll();
    }

    @GetMapping("/{id}")
    public Patient get(@PathVariable long id) {
        return patientRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Paciente no encontrado: " + id));
    }

    public record CreatePatientRequest(Long id, @NotBlank String name, String phone, String city, Boolean active) {}

    @PostMapping
    public ResponseEntity<Patient> create(@Valid @RequestBody CreatePatientRequest r) {
        Long id = r.id();
        if (id == null || id <= 0) {
            id = patientRepo.findAll().stream().mapToLong(Patient::getId).max().orElse(1000L) + 1;
        }
        if (patientRepo.existsById(id)) {
            throw new IllegalStateException("Ya existe un paciente con el ID " + id);
        }
        Patient p = new Patient(id, r.name(), r.phone() == null ? "" : r.phone(), r.city() == null ? "Monterrey" : r.city(), r.active() == null ? true : r.active());
        return ResponseEntity.status(HttpStatus.CREATED).body(patientRepo.save(p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (!patientRepo.existsById(id)) {
            throw new NoSuchElementException("Paciente no encontrado: " + id);
        }
        if (appointmentRepo.existsByPatientId(id)) {
            throw new IllegalStateException("No es posible eliminar el paciente porque tiene citas registradas en el sistema");
        }
        patientRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

