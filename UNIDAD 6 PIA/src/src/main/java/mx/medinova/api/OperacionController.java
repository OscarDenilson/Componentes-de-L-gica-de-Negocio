package mx.medinova.api;

import mx.medinova.application.OperacionService;
import mx.medinova.domain.Operacion;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operaciones")
public class OperacionController {
    private final OperacionService service;

    public OperacionController(OperacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Operacion> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Operacion get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/{id}/aprobar")
    public Operacion aprobar(@PathVariable String id) {
        return service.aprobar(id);
    }

    @PostMapping("/{id}/completar")
    public Operacion completar(@PathVariable String id) {
        return service.completar(id);
    }

    @PostMapping("/{id}/autorizar")
    public Operacion autorizar(@PathVariable String id) {
        return service.autorizar(id);
    }

    @PostMapping("/{id}/desautorizar")
    public Operacion desautorizar(@PathVariable String id) {
        return service.desautorizar(id);
    }

    public record CancelarRequest(String motivo) {}

    @PostMapping("/{id}/cancelar")
    public Operacion cancelar(@PathVariable String id, @RequestBody(required = false) CancelarRequest req) {
        String motivo = req != null && req.motivo() != null ? req.motivo() : "Cancelada desde API";
        return service.cancelar(id, motivo);
    }

    @GetMapping("/{id}/evaluar")
    public OperacionService.EvaluacionDTO evaluar(@PathVariable String id) {
        return service.evaluar(id);
    }
}
