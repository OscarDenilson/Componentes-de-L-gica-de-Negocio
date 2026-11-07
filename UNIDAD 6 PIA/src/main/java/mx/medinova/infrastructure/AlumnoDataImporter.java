package mx.medinova.infrastructure;

import mx.medinova.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

@Component
public class AlumnoDataImporter {
    private static final Logger log = LoggerFactory.getLogger(AlumnoDataImporter.class);

    private final ActorRepository actorRepo;
    private final RecursoRepository recursoRepo;
    private final OperacionRepository operacionRepo;

    public AlumnoDataImporter(ActorRepository actorRepo, RecursoRepository recursoRepo, OperacionRepository operacionRepo) {
        this.actorRepo = actorRepo;
        this.recursoRepo = recursoRepo;
        this.operacionRepo = operacionRepo;
    }

    public void importIfEmpty() {
        if (operacionRepo.count() > 0) {
            log.info("Datos de Alumno ya importados anteriormente.");
            return;
        }

        try {
            log.info("Iniciando importación de datos académicos de 01_Alumno...");
            Map<String, Recurso> recursoMap = importCatalogo();
            importActores();
            importOperacionesYDetalle(recursoMap);
            log.info("Importación completada con éxito. Operaciones cargadas: {}", operacionRepo.count());
        } catch (Exception e) {
            log.error("Error importando datos de alumno: {}", e.getMessage(), e);
        }
    }

    private InputStream openFileStream(String filename) throws IOException {
        Path p = Path.of("data", filename);
        if (Files.exists(p)) {
            return Files.newInputStream(p);
        }
        InputStream is = getClass().getResourceAsStream("/data/" + filename);
        if (is != null) return is;
        throw new FileNotFoundException("No se encontró archivo " + filename + " en /data ni en classpath.");
    }

    private List<String> readLines(String filename) throws IOException {
        try (InputStream is = openFileStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    if (line.startsWith("\uFEFF")) line = line.substring(1);
                    first = false;
                }
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
            return lines;
        }
    }

    private Map<String, Recurso> importCatalogo() throws IOException {
        List<String> lines = readLines("catalogo.csv");
        Map<String, Recurso> map = new HashMap<>();
        // Header: recurso_id,descripcion,precio_mxn,capacidad_o_stock,habilitado
        for (int i = 1; i < lines.size(); i++) {
            String[] cols = lines.get(i).split(",");
            if (cols.length >= 5) {
                String id = cols[0].trim();
                String desc = cols[1].trim();
                BigDecimal precio = new BigDecimal(cols[2].trim());
                int cap = Integer.parseInt(cols[3].trim());
                boolean hab = "1".equals(cols[4].trim()) || "true".equalsIgnoreCase(cols[4].trim());
                Recurso r = new Recurso(id, desc, precio, cap, hab);
                map.put(id, r);
            }
        }
        recursoRepo.saveAll(map.values());
        return map;
    }

    private void importActores() throws IOException {
        List<String> lines = readLines("actores.csv");
        List<Actor> actores = new ArrayList<>();
        // Header: actor_id,nombre,es_preferente,activo,ciudad
        for (int i = 1; i < lines.size(); i++) {
            String[] cols = lines.get(i).split(",");
            if (cols.length >= 5) {
                String id = cols[0].trim();
                String nombre = cols[1].trim();
                boolean pref = "1".equals(cols[2].trim()) || "true".equalsIgnoreCase(cols[2].trim());
                boolean activo = "1".equals(cols[3].trim()) || "true".equalsIgnoreCase(cols[3].trim());
                String ciudad = cols[4].trim();
                actores.add(new Actor(id, nombre, pref, activo, ciudad));
            }
        }
        actorRepo.saveAll(actores);
    }

    private void importOperacionesYDetalle(Map<String, Recurso> recursoMap) throws IOException {
        List<String> opLines = readLines("operaciones.csv");
        Map<String, Operacion> opMap = new LinkedHashMap<>();
        // Header: operacion_id,actor_id,estado_inicial,fecha_solicitud,promocion_activa,autorizado,referencia
        for (int i = 1; i < opLines.size(); i++) {
            String[] cols = opLines.get(i).split(",");
            if (cols.length >= 7) {
                String opId = cols[0].trim();
                String actorId = cols[1].trim();
                LocalDate fecha = LocalDate.parse(cols[3].trim());
                boolean promo = "1".equals(cols[4].trim()) || "true".equalsIgnoreCase(cols[4].trim());
                boolean auth = "1".equals(cols[5].trim()) || "true".equalsIgnoreCase(cols[5].trim());
                String ref = cols[6].trim();

                Operacion op = new Operacion(opId, actorId, fecha, promo, auth, ref);
                opMap.put(opId, op);
            }
        }

        List<String> detLines = readLines("detalle_operaciones.csv");
        // Header: operacion_id,recurso_id,cantidad
        for (int i = 1; i < detLines.size(); i++) {
            String[] cols = detLines.get(i).split(",");
            if (cols.length >= 3) {
                String opId = cols[0].trim();
                String recId = cols[1].trim();
                int cant = Integer.parseInt(cols[2].trim());

                Operacion op = opMap.get(opId);
                Recurso rec = recursoMap.get(recId);
                BigDecimal precioUnit = rec != null ? rec.getPrecioMxn() : BigDecimal.ZERO;

                if (op != null) {
                    op.agregarLinea(new DetalleOperacion(opId, recId, cant, precioUnit));
                }
            }
        }

        // Recalcular totales con BigDecimal y guardar
        for (Operacion op : opMap.values()) {
            op.recalcularTotal();
        }
        operacionRepo.saveAll(opMap.values());
    }
}

