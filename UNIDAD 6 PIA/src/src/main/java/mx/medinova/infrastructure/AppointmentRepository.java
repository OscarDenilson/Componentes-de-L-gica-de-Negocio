package mx.medinova.infrastructure;

import mx.medinova.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByStartAtDesc(Long patientId);
    boolean existsByDoctorIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(Long doctorId, List<AppointmentStatus> statuses, LocalDateTime end, LocalDateTime start);
    boolean existsByPatientId(Long patientId);
    boolean existsByDoctorId(Long doctorId);
}
