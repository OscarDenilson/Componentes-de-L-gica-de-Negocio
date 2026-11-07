package mx.medinova.api;

import mx.medinova.domain.Doctor;
import mx.medinova.infrastructure.AppointmentRepository;
import mx.medinova.infrastructure.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorRepository doctorRepo;
    private final AppointmentRepository appointmentRepo;

    public DoctorController(DoctorRepository doctorRepo, AppointmentRepository appointmentRepo) {
        this.doctorRepo = doctorRepo;
        this.appointmentRepo = appointmentRepo;
    }

    @GetMapping
    public List<Doctor> all() {
        return doctorRepo.findAll();
    }

    @GetMapping("/{id}")
    public Doctor get(@PathVariable long id) {
        return doctorRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Médico no encontrado: " + id));
    }

    public record CreateDoctorRequest(Long id, @NotBlank String name, @NotBlank String specialty, Long branchId, Boolean active) {}

    @PostMapping
    public ResponseEntity<Doctor> create(@Valid @RequestBody CreateDoctorRequest r) {
        Long id = r.id();
        if (id == null || id <= 0) {
            id = doctorRepo.findAll().stream().mapToLong(Doctor::getId).max().orElse(200L) + 1;
        }
        if (doctorRepo.existsById(id)) {
            throw new IllegalStateException("Ya existe un médico con el ID " + id);
        }
        Doctor d = new Doctor(id, r.name(), r.specialty(), r.branchId() == null ? 1L : r.branchId(), r.active() == null ? true : r.active());
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorRepo.save(d));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (!doctorRepo.existsById(id)) {
            throw new NoSuchElementException("Médico no encontrado: " + id);
        }
        if (appointmentRepo.existsByDoctorId(id)) {
            throw new IllegalStateException("No es posible eliminar el médico porque tiene citas registradas en el sistema");
        }
        doctorRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

