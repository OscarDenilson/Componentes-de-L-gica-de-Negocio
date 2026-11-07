package mx.medinova;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperacionesApiIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void testActoresEndpoint() throws Exception {
        mvc.perform(get("/api/actores"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(40)));
    }

    @Test
    void testRecursosEndpoint() throws Exception {
        mvc.perform(get("/api/recursos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(20)));
    }

    @Test
    void testOperacionesListAndGet() throws Exception {
        mvc.perform(get("/api/operaciones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(60)));

        mvc.perform(get("/api/operaciones/CITA-O001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.operacionId").value("CITA-O001"))
            .andExpect(jsonPath("$.actorId").value("CITA-C009"))
            .andExpect(jsonPath("$.lineas").isArray());
    }

    @Test
    void testEvaluarOperacion() throws Exception {
        mvc.perform(get("/api/operaciones/CITA-O001/evaluar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.operacionId").value("CITA-O001"))
            .andExpect(jsonPath("$.puedeProcesarse").isBoolean())
            .andExpect(jsonPath("$.beneficioReferencia").isBoolean());
    }

    @Test
    void testCasosPruebaEndpoint() throws Exception {
        mvc.perform(get("/api/pruebas/casos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(120)));
    }

    @Test
    void testTransicionesPruebaEndpoint() throws Exception {
        mvc.perform(get("/api/pruebas/transiciones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(20)));
    }

    @Test
    void testAutorizarYDesautorizarOperacion() throws Exception {
        mvc.perform(post("/api/operaciones/CITA-O008/autorizar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.operacionId").value("CITA-O008"))
            .andExpect(jsonPath("$.autorizado").value(true));

        mvc.perform(post("/api/operaciones/CITA-O008/desautorizar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.operacionId").value("CITA-O008"))
            .andExpect(jsonPath("$.autorizado").value(false));
    }
}

