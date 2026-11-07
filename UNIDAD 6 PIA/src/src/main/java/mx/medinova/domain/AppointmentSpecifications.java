package mx.medinova.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class AppointmentSpecifications {
    private AppointmentSpecifications() {}

    public record BookingRequest(
        Long patientId,
        Long doctorId,
        Long branchId,
        String specialty,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean patientActive,
        boolean doctorActive,
        boolean branchActive,
        boolean insurerAuthorizationRequired,
        boolean insurerAuthorized,
        String operacionId
    ) {
        public BookingRequest(Long patientId, Long doctorId, Long branchId, String specialty, LocalDateTime startAt, LocalDateTime endAt, boolean patientActive, boolean doctorActive, boolean branchActive, boolean insurerAuthorizationRequired, boolean insurerAuthorized) {
            this(patientId, doctorId, branchId, specialty, startAt, endAt, patientActive, doctorActive, branchActive, insurerAuthorizationRequired, insurerAuthorized, null);
        }
    }

    public static Specification<BookingRequest> activePatient = BookingRequest::patientActive;
    public static Specification<BookingRequest> activeDoctor = BookingRequest::doctorActive;
    public static Specification<BookingRequest> activeBranch = BookingRequest::branchActive;
    public static Specification<BookingRequest> authorizationSatisfied = r -> !r.insurerAuthorizationRequired() || r.insurerAuthorized();
    public static Specification<BookingRequest> validInterval = r -> r.startAt() != null && r.endAt() != null && r.endAt().isAfter(r.startAt());

    public static Specification<BookingRequest> canBook() {
        return activePatient.andSpec(activeDoctor).andSpec(activeBranch).andSpec(authorizationSatisfied).andSpec(validInterval);
    }

    public static Specification<BookingRequest> needsManualReview() {
        return activePatient.andSpec(activeDoctor).andSpec(authorizationSatisfied.not().orSpec(validInterval.not()));
    }
}
