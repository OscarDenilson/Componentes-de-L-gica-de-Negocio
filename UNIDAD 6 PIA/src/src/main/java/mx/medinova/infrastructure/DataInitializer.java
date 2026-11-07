package mx.medinova.infrastructure;

import mx.medinova.domain.Doctor;
import mx.medinova.domain.Patient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final AlumnoDataImporter alumnoDataImporter;

    public DataInitializer(PatientRepository patientRepo, DoctorRepository doctorRepo, AlumnoDataImporter alumnoDataImporter) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.alumnoDataImporter = alumnoDataImporter;
    }

    @Override
    public void run(String... args) {
        alumnoDataImporter.importIfEmpty();

        if (patientRepo.count() == 0) {
            patientRepo.save(new Patient(1001L, "María Elena Garza", "818-101-2001", "Monterrey", true));
            patientRepo.save(new Patient(1002L, "Carlos Villarreal", "818-101-2002", "Apodaca", true));
            patientRepo.save(new Patient(1003L, "Ana Lucía Rodríguez", "818-101-2003", "Monterrey", true));
            patientRepo.save(new Patient(1004L, "Roberto Treviño", "818-101-2004", "Apodaca", true));
            patientRepo.save(new Patient(1005L, "Sofía Cantú", "818-101-2005", "Monterrey", true));
            patientRepo.save(new Patient(1006L, "Alejandro Gómez", "818-101-2006", "San Pedro", true));
        }
        if (doctorRepo.count() == 0) {
            doctorRepo.save(new Doctor(201L, "Dra. Laura Morales", "Medicina General", 1L, true));
            doctorRepo.save(new Doctor(202L, "Dr. Héctor Salinas", "Cardiología", 1L, true));
            doctorRepo.save(new Doctor(203L, "Dra. Patricia Wong", "Dermatología", 2L, true));
            doctorRepo.save(new Doctor(204L, "Dr. Fernando Elizondo", "Pediatría", 1L, true));
            doctorRepo.save(new Doctor(205L, "Dra. Mónica Flores", "Traumatología", 2L, true));
        }
    }
}
