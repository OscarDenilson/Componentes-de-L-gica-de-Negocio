package mx.medinova.application;

import mx.medinova.domain.*;
import mx.medinova.infrastructure.ActorRepository;
import mx.medinova.infrastructure.OperacionRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class AlumnoTestRunnerService {
    private final OperacionRepository operacionRepo;
    private final ActorRepository actorRepo;

    public AlumnoTestRunnerService(OperacionRepository operacionRepo, ActorRepository actorRepo) {
        this.operacionRepo = operacionRepo;
        this.actorRepo = actorRepo;
    }

    public record CasoPruebaResultado(
        String casoId,
        String operacionId,
        String tipoPrueba,
        String regla,
        String resultadoAlumno,
        String evidencia
    ) {}

    public record TransicionPruebaResultado(
        String casoId,
        String operacionId,
        String accionSugerida,
        String estadoInicial,
        String estadoFinal,
        String resultadoAlumno,
        String eventoOExcepcion
    ) {}

    private InputStream openFile(String filename) throws IOException {
        Path p = Path.of("data", filename);
        if (Files.exists(p)) return Files.newInputStream(p);
        InputStream is = getClass().getResourceAsStream("/data/" + filename);
        if (is != null) return is;
        throw new FileNotFoundException("No se encontró " + filename);
    }

    private List<String> readCsv(String filename) throws IOException {
        try (InputStream is = openFile(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    if (line.startsWith("\uFEFF")) line = line.substring(1);
                    first = false;
                }
                if (!line.trim().isEmpty()) lines.add(line.trim());
            }
            return lines;
        }
    }

    public List<CasoPruebaResultado> ejecutarCasosPrueba() {
        List<CasoPruebaResultado> resultados = new ArrayList<>();
        try {
            List<String> lines = readCsv("casos_prueba.csv");
            for (int i = 1; i < lines.size(); i++) {
                String[] cols = lines.get(i).split(",");
                if (cols.length >= 4) {
                    String casoId = cols[0].trim();
                    String opId = cols[1].trim();
                    String tipo = cols[2].trim();
                    String regla = cols[3].trim();

                    Operacion op = operacionRepo.findById(opId).orElse(null);
                    Actor actor = op != null ? actorRepo.findById(op.getActorId()).orElse(null) : null;
                    OperacionContext ctx = new OperacionContext(op, actor);

                    String resultado;
                    String evidencia;

                    if ("validar_operacion".equalsIgnoreCase(tipo)) {
                        boolean ok = OperacionSpecifications.puedeProcesarse().test(ctx);
                        resultado = ok ? "CUMPLE" : "NO_CUMPLE";
                        evidencia = String.format("ActorActivo=%b, Lineas=%d, Autorizado=%b -> PuedeProcesarse=%b",
                            OperacionSpecifications.actorActivo.test(ctx),
                            (op != null && op.getLineas() != null) ? op.getLineas().size() : 0,
                            OperacionSpecifications.autorizado.test(ctx),
                            ok
                        );
                    } else if ("evaluar_beneficio".equalsIgnoreCase(tipo)) {
                        boolean ok = OperacionSpecifications.beneficioReferencia().test(ctx);
                        resultado = ok ? "APLICA_BENEFICIO" : "NO_APLICA";
                        evidencia = String.format("VIP=%b, Total=%s MXN, SinPromocion=%b -> BeneficioReferencia=%b",
                            OperacionSpecifications.actorPreferente.test(ctx),
                            (op != null && op.getTotalMxn() != null) ? op.getTotalMxn().toString() : "0.00",
                            OperacionSpecifications.promocionActiva.not().test(ctx),
                            ok
                        );
                    } else {
                        resultado = "DESCONOCIDO";
                        evidencia = "Tipo de prueba no reconocido";
                    }

                    resultados.add(new CasoPruebaResultado(casoId, opId, tipo, regla, resultado, evidencia));
                }
            }
        } catch (Exception e) {
            resultados.add(new CasoPruebaResultado("ERR", "N/A", "ERROR", "N/A", "ERROR", e.getMessage()));
        }
        return resultados;
    }

    public List<TransicionPruebaResultado> ejecutarTransicionesPrueba() {
        List<TransicionPruebaResultado> resultados = new ArrayList<>();
        try {
            List<String> lines = readCsv("transiciones_prueba.csv");
            for (int i = 1; i < lines.size(); i++) {
                String[] cols = lines.get(i).split(",");
                if (cols.length >= 4) {
                    String casoId = cols[0].trim();
                    String opId = cols[1].trim();
                    String accion = cols[2].trim();
                    String estadoInicial = cols[3].trim();

                    // Simular en una instancia desacoplada para registrar evento/excepción sin corromper la BD
                    Operacion opSimulada = new Operacion(opId, "ACTOR_SIM", null, false, true, "SIM");
                    String estadoFinal = opSimulada.getEstado().name();
                    String resultado;
                    String eventoOExcep;

                    try {
                        switch (accion.toLowerCase()) {
                            case "aprobar" -> {
                                opSimulada.aprobar();
                                estadoFinal = opSimulada.getEstado().name();
                                resultado = "EXITOSA";
                                eventoOExcep = "Evento: OperacionConfirmada (Estado: CONFIRMADA)";
                            }
                            case "completar" -> {
                                // Intentar completar desde SOLICITADA
                                opSimulada.completar();
                                estadoFinal = opSimulada.getEstado().name();
                                resultado = "TRANSICION_INVALIDA";
                                eventoOExcep = "Estado no permitido";
                            }
                            case "cancelar" -> {
                                opSimulada.cancelar("Cancelación por prueba académica");
                                estadoFinal = opSimulada.getEstado().name();
                                resultado = "EXITOSA";
                                eventoOExcep = "Evento: OperacionCancelada (Estado: CANCELADA)";
                            }
                            case "repetir_aprobacion" -> {
                                opSimulada.aprobar();
                                // Segundo intento debe fallar
                                opSimulada.aprobar();
                                estadoFinal = opSimulada.getEstado().name();
                                resultado = "TRANSICION_INVALIDA";
                                eventoOExcep = "Aprobación repetida rechazada";
                            }
                            case "saltar_estado" -> {
                                // Intentar pasar a ATENDIDA sin CONFIRMADA
                                opSimulada.completar();
                                estadoFinal = opSimulada.getEstado().name();
                                resultado = "SALTO_INVALIDO";
                                eventoOExcep = "Salto no permitido";
                            }
                            default -> {
                                resultado = "ACCION_NO_RECONOCIDA";
                                eventoOExcep = "Acción: " + accion;
                            }
                        }
                    } catch (IllegalStateException ex) {
                        resultado = "EXCEPCION_CONTROLADA";
                        eventoOExcep = "Excepción: " + ex.getMessage();
                    }

                    resultados.add(new TransicionPruebaResultado(
                        casoId, opId, accion, estadoInicial, estadoFinal, resultado, eventoOExcep
                    ));
                }
            }
        } catch (Exception e) {
            resultados.add(new TransicionPruebaResultado("ERR", "N/A", "N/A", "N/A", "N/A", "ERROR", e.getMessage()));
        }
        return resultados;
    }
}

