package mx.medinova.application;

import mx.medinova.domain.*;
import mx.medinova.infrastructure.ActorRepository;
import mx.medinova.infrastructure.OperacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OperacionService {
    private final OperacionRepository operacionRepo;
    private final ActorRepository actorRepo;

    public OperacionService(OperacionRepository operacionRepo, ActorRepository actorRepo) {
        this.operacionRepo = operacionRepo;
        this.actorRepo = actorRepo;
    }

    public List<Operacion> all() {
        return operacionRepo.findAll();
    }

    public Operacion get(String id) {
        return operacionRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Operación no encontrada: " + id));
    }

    @Transactional
    public Operacion aprobar(String id) {
        Operacion op = get(id);
        op.aprobar();
        return operacionRepo.save(op);
    }

    @Transactional
    public Operacion completar(String id) {
        Operacion op = get(id);
        op.completar();
        return operacionRepo.save(op);
    }

    @Transactional
    public Operacion cancelar(String id, String motivo) {
        Operacion op = get(id);
        op.cancelar(motivo);
        return operacionRepo.save(op);
    }

    @Transactional
    public Operacion autorizar(String id) {
        Operacion op = get(id);
        op.setAutorizado(true);
        return operacionRepo.save(op);
    }

    @Transactional
    public Operacion desautorizar(String id) {
        Operacion op = get(id);
        op.setAutorizado(false);
        return operacionRepo.save(op);
    }

    public record EvaluacionDTO(
        String operacionId,
        String actorId,
        BigDecimal totalMxn,
        boolean actorActivo,
        boolean detalleNoVacio,
        boolean autorizado,
        boolean puedeProcesarse,
        boolean actorPreferente,
        boolean totalMayorOIgual1500,
        boolean sinPromocion,
        boolean beneficioReferencia
    ) {}

    public EvaluacionDTO evaluar(String id) {
        Operacion op = get(id);
        Actor actor = actorRepo.findById(op.getActorId()).orElse(null);
        OperacionContext ctx = new OperacionContext(op, actor);

        boolean actAct = OperacionSpecifications.actorActivo.test(ctx);
        boolean detNv = OperacionSpecifications.detalleNoVacio.test(ctx);
        boolean auth = OperacionSpecifications.autorizado.test(ctx);
        boolean proc = OperacionSpecifications.puedeProcesarse().test(ctx);

        boolean pref = OperacionSpecifications.actorPreferente.test(ctx);
        boolean t1500 = OperacionSpecifications.totalMinimo1500.test(ctx);
        boolean sinPromo = OperacionSpecifications.promocionActiva.not().test(ctx);
        boolean benef = OperacionSpecifications.beneficioReferencia().test(ctx);

        return new EvaluacionDTO(
            op.getOperacionId(),
            op.getActorId(),
            op.getTotalMxn(),
            actAct,
            detNv,
            auth,
            proc,
            pref,
            t1500,
            sinPromo,
            benef
        );
    }
}
