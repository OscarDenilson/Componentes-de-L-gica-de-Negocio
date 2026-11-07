package mx.medinova.api;

import mx.medinova.application.AlumnoTestRunnerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pruebas")
public class PruebasAlumnoController {
    private final AlumnoTestRunnerService runner;

    public PruebasAlumnoController(AlumnoTestRunnerService runner) {
        this.runner = runner;
    }

    @GetMapping("/casos")
    public List<AlumnoTestRunnerService.CasoPruebaResultado> casos() {
        return runner.ejecutarCasosPrueba();
    }

    @GetMapping("/transiciones")
    public List<AlumnoTestRunnerService.TransicionPruebaResultado> transiciones() {
        return runner.ejecutarTransicionesPrueba();
    }
}

