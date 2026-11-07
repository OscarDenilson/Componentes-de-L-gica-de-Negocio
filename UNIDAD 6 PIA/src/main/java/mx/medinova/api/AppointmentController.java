package mx.medinova.api;

import mx.medinova.application.AppointmentService;
import mx.medinova.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;

    public AppointmentController(AppointmentService s) {
        service = s;
    }

    @GetMapping
    public List<Appointment> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Appointment get(@PathVariable long id) {
        return service.get(id);
    }

    public record CreateRequest(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        @NotNull Long branchId,
        @NotBlank String specialty,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        @NotBlank String reason,
        boolean patientActive,
        boolean doctorActive,
        boolean branchActive,
        boolean insurerAuthorizationRequired,
        boolean insurerAuthorized,
        String operacionId
    ) {
        public CreateRequest(Long patientId, Long doctorId, Long branchId, String specialty, LocalDateTime startAt, LocalDateTime endAt, String reason, boolean patientActive, boolean doctorActive, boolean branchActive, boolean insurerAuthorizationRequired, boolean insurerAuthorized) {
            this(patientId, doctorId, branchId, specialty, startAt, endAt, reason, patientActive, doctorActive, branchActive, insurerAuthorizationRequired, insurerAuthorized, null);
        }
    }

    @PostMapping
    public ResponseEntity<Appointment> create(@Valid @RequestBody CreateRequest r) {
        var b = new AppointmentSpecifications.BookingRequest(
            r.patientId(), r.doctorId(), r.branchId(), r.specialty(),
            r.startAt(), r.endAt(), r.patientActive(), r.doctorActive(),
            r.branchActive(), r.insurerAuthorizationRequired(), r.insurerAuthorized(),
            r.operacionId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(b, r.reason()));
    }

    public record ArrivalRequest(@NotNull LocalDateTime arrivedAt) {}

    @PostMapping("/{id}/arrival")
    public Appointment arrival(@PathVariable long id, @Valid @RequestBody ArrivalRequest r) {
        return service.arrival(id, r.arrivedAt());
    }

    public record DecisionRequest(@NotNull Long doctorId, @NotNull MedicalDecision decision, String notes) {}

    @PostMapping("/{id}/decision")
    public Appointment decision(@PathVariable long id, @Valid @RequestBody DecisionRequest r) {
        return service.decide(id, r.doctorId(), r.decision(), r.notes());
    }

    public record TextRequest(String reason) {}

    @PostMapping("/{id}/confirm")
    public Appointment confirm(@PathVariable long id) {
        return service.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    public Appointment cancel(@PathVariable long id, @RequestBody(required = false) TextRequest r) {
        return service.cancel(id, r == null ? "Cancelación" : r.reason());
    }

    @PostMapping("/{id}/no-show")
    public Appointment noShow(@PathVariable long id) {
        return service.noShow(id);
    }
}
