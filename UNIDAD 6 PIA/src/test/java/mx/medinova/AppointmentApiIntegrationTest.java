package mx.medinova;

import mx.medinova.infrastructure.AppointmentRepository;
import mx.medinova.infrastructure.DoctorRepository;
import mx.medinova.infrastructure.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired AppointmentRepository repo;
    @Autowired PatientRepository patientRepo;
    @Autowired DoctorRepository doctorRepo;

    @Test
    void listEndpointRespondsOk() throws Exception {
        mvc.perform(get("/api/appointments")).andExpect(status().isOk());
    }

    @Test
    void unknownAppointmentReturns404() throws Exception {
        mvc.perform(get("/api/appointments/99999")).andExpect(status().isNotFound());
    }

    @Test
    void homeRedirectsToIndex() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection());
    }

    @Test
    void staticIndexServesOk() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk());
    }

    @Test
    void listPatientsAndDoctorsOk() throws Exception {
        mvc.perform(get("/api/patients")).andExpect(status().isOk());
        mvc.perform(get("/api/doctors")).andExpect(status().isOk());
    }

    @Test
    void createAndGetAndDeletePatientOk() throws Exception {
        String json = """
            {
              "id": 9991,
              "name": "Paciente Test",
              "phone": "818-999-9991",
              "city": "Monterrey",
              "active": true
            }
            """;
        mvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(9991))
            .andExpect(jsonPath("$.name").value("Paciente Test"));

        mvc.perform(get("/api/patients/9991"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Paciente Test"));

        mvc.perform(delete("/api/patients/9991"))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/patients/9991"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createAndGetAndDeleteDoctorOk() throws Exception {
        String json = """
            {
              "id": 9992,
              "name": "Dr. Test",
              "specialty": "Neurología",
              "branchId": 1,
              "active": true
            }
            """;
        mvc.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(9992))
            .andExpect(jsonPath("$.name").value("Dr. Test"));

        mvc.perform(get("/api/doctors/9992"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Dr. Test"));

        mvc.perform(delete("/api/doctors/9992"))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/doctors/9992"))
            .andExpect(status().isNotFound());
    }

    @Test
    void rejectAppointmentWhenPatientDoesNotExist() throws Exception {
        String json = """
            {
              "patientId": 88888,
              "doctorId": 201,
              "branchId": 1,
              "specialty": "Medicina General",
              "startAt": "2026-11-10T10:00:00",
              "endAt": "2026-11-10T10:30:00",
              "reason": "Consulta",
              "patientActive": true,
              "doctorActive": true,
              "branchActive": true,
              "insurerAuthorizationRequired": false,
              "insurerAuthorized": false
            }
            """;
        mvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("El paciente con ID 88888 no existe")));
    }

    @Test
    void rejectAppointmentWhenDoctorDoesNotExist() throws Exception {
        String json = """
            {
              "patientId": 1001,
              "doctorId": 77777,
              "branchId": 1,
              "specialty": "Medicina General",
              "startAt": "2026-11-10T10:00:00",
              "endAt": "2026-11-10T10:30:00",
              "reason": "Consulta",
              "patientActive": true,
              "doctorActive": true,
              "branchActive": true,
              "insurerAuthorizationRequired": false,
              "insurerAuthorized": false
            }
            """;
        mvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("El médico con ID 77777 no existe")));
    }

    @Test
    void rejectAppointmentWhenSpecialtyDoesNotExistOrMismatch() throws Exception {
        String json = """
            {
              "patientId": 1001,
              "doctorId": 201,
              "branchId": 1,
              "specialty": "Acupuntura Espacial",
              "startAt": "2026-11-10T10:00:00",
              "endAt": "2026-11-10T10:30:00",
              "reason": "Consulta",
              "patientActive": true,
              "doctorActive": true,
              "branchActive": true,
              "insurerAuthorizationRequired": false,
              "insurerAuthorized": false
            }
            """;
        mvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("La especialidad 'Acupuntura Espacial' no existe o no corresponde")));
    }

    @Test
    void createAppointmentWithValidIdsSucceeds() throws Exception {
        String json = """
            {
              "patientId": 1001,
              "doctorId": 201,
              "branchId": 1,
              "specialty": "Medicina General",
              "startAt": "2026-11-12T08:00:00",
              "endAt": "2026-11-12T08:30:00",
              "reason": "Chequeo médico de rutina",
              "patientActive": true,
              "doctorActive": true,
              "branchActive": true,
              "insurerAuthorizationRequired": false,
              "insurerAuthorized": false
            }
            """;
        mvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.patientId").value(1001))
            .andExpect(jsonPath("$.doctorId").value(201));
    }

    @Test
    void rejectAppointmentWhenOperacionNotAuthorized() throws Exception {
        // CITA-O016 has autorizado = 0
        String json = """
            {
              "patientId": 1001,
              "doctorId": 201,
              "branchId": 1,
              "specialty": "Medicina General",
              "operacionId": "CITA-O016",
              "startAt": "2026-11-12T09:00:00",
              "endAt": "2026-11-12T09:30:00",
              "reason": "Consulta con operacion no autorizada",
              "patientActive": true,
              "doctorActive": true,
              "branchActive": true,
              "insurerAuthorizationRequired": false,
              "insurerAuthorized": false
            }
            """;
        mvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("no está autorizada")));
    }

    @Test
    void createAppointmentWithAuthorizedOperacionSucceeds() throws Exception {
        // CITA-O001 has autorizado = 1
        String json = """
            {
              "patientId": 1001,
              "doctorId": 201,
              "branchId": 1,
              "specialty": "Medicina General",
              "operacionId": "CITA-O001",
              "startAt": "2026-11-12T11:00:00",
              "endAt": "2026-11-12T11:30:00",
              "reason": "Consulta con operacion autorizada",
              "patientActive": true,
              "doctorActive": true,
              "branchActive": true,
              "insurerAuthorizationRequired": false,
              "insurerAuthorized": false
            }
            """;
        mvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.operacionId").value("CITA-O001"));
    }
}
