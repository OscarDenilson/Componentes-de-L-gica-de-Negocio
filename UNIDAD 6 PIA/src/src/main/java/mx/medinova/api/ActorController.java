package mx.medinova.api;

import mx.medinova.domain.Actor;
import mx.medinova.infrastructure.ActorRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/actores")
public class ActorController {
    private final ActorRepository repo;

    public ActorController(ActorRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Actor> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Actor get(@PathVariable String id) {
        return repo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Actor no encontrado: " + id));
    }
}

