package mx.medinova.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PatientAndDoctorTest {

    @Test
    void patientGettersAndSetters() {
        Patient p = new Patient(1001L, "María Garza", "818-111", "Monterrey", true);
        assertEquals(1001L, p.getId());
        assertEquals("María Garza", p.getName());
        assertEquals("818-111", p.getPhone());
        assertEquals("Monterrey", p.getCity());
        assertTrue(p.isActive());

        p.setName("María G.");
        p.setPhone("818-222");
        p.setCity("San Pedro");
        p.setActive(false);

        assertEquals("María G.", p.getName());
        assertEquals("818-222", p.getPhone());
        assertEquals("San Pedro", p.getCity());
        assertFalse(p.isActive());
    }

    @Test
    void patientValidations() {
        assertThrows(IllegalArgumentException.class, () -> new Patient(null, "Test", "123", "City", true));
        assertThrows(IllegalArgumentException.class, () -> new Patient(0L, "Test", "123", "City", true));
        assertThrows(IllegalArgumentException.class, () -> new Patient(1L, "", "123", "City", true));
    }

    @Test
    void doctorGettersAndSetters() {
        Doctor d = new Doctor(201L, "Dra. Laura", "Medicina General", 1L, true);
        assertEquals(201L, d.getId());
        assertEquals("Dra. Laura", d.getName());
        assertEquals("Medicina General", d.getSpecialty());
        assertEquals(1L, d.getBranchId());
        assertTrue(d.isActive());

        d.setName("Dra. L. Morales");
        d.setSpecialty("Cardiología");
        d.setBranchId(2L);
        d.setActive(false);

        assertEquals("Dra. L. Morales", d.getName());
        assertEquals("Cardiología", d.getSpecialty());
        assertEquals(2L, d.getBranchId());
        assertFalse(d.isActive());
    }

    @Test
    void doctorValidations() {
        assertThrows(IllegalArgumentException.class, () -> new Doctor(null, "Doc", "Spec", 1L, true));
        assertThrows(IllegalArgumentException.class, () -> new Doctor(0L, "Doc", "Spec", 1L, true));
        assertThrows(IllegalArgumentException.class, () -> new Doctor(1L, "", "Spec", 1L, true));
        assertThrows(IllegalArgumentException.class, () -> new Doctor(1L, "Doc", "", 1L, true));
    }
}

