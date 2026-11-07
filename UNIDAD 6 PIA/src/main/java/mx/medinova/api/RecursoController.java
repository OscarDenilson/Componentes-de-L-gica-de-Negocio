package mx.medinova.api;

import mx.medinova.domain.Recurso;
import mx.medinova.infrastructure.RecursoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/recursos")
public class RecursoController {
    private final RecursoRepository repo;

    public RecursoController(RecursoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Recurso> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Recurso get(@PathVariable String id) {
        return repo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Recurso no encontrado: " + id));
    }
}

