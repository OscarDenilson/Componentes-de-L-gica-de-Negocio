package mx.medinova.domain;

import java.math.BigDecimal;

public final class OperacionSpecifications {
    private OperacionSpecifications() {}

    // Specification 1: Actor Activo
    public static Specification<OperacionContext> actorActivo = ctx ->
        ctx != null && ctx.actor() != null && ctx.actor().isActivo();

    // Specification 2: Detalle No Vacío (al menos una línea de detalle)
    public static Specification<OperacionContext> detalleNoVacio = ctx ->
        ctx != null && ctx.operacion() != null && ctx.operacion().getLineas() != null && !ctx.operacion().getLineas().isEmpty();

    // Specification 3: Autorización Válida
    public static Specification<OperacionContext> autorizado = ctx ->
        ctx != null && ctx.operacion() != null && ctx.operacion().isAutorizado();

    // Specification 4: Actor Preferente (VIP)
    public static Specification<OperacionContext> actorPreferente = ctx ->
        ctx != null && ctx.actor() != null && ctx.actor().isEsPreferente();

    // Specification 5: Total Mínimo 1500 MXN
    public static Specification<OperacionContext> totalMinimo1500 = ctx ->
        ctx != null && ctx.operacion() != null && ctx.operacion().getTotalMxn() != null &&
        ctx.operacion().getTotalMxn().compareTo(new BigDecimal("1500.00")) >= 0;

    // Specification 6: Promoción Activa
    public static Specification<OperacionContext> promocionActiva = ctx ->
        ctx != null && ctx.operacion() != null && ctx.operacion().isPromocionActiva();

    /**
     * Regla 1 (puede_procesarse):
     * actor activo AND operacion con al menos una linea AND autorizacion
     */
    public static Specification<OperacionContext> puedeProcesarse() {
        return actorActivo.andSpec(detalleNoVacio).andSpec(autorizado);
    }

    /**
     * Regla 2 (beneficio_referencia):
     * actor preferente (VIP) AND total >= 1500 MXN AND NOT promocion activa
     */
    public static Specification<OperacionContext> beneficioReferencia() {
        return actorPreferente.andSpec(totalMinimo1500).andSpec(promocionActiva.not());
    }

    /**
     * Regla alternativa de revisión manual (Demuestra AND/OR/NOT):
     * actor activo AND (NOT autorizado OR NOT detalleNoVacio)
     */
    public static Specification<OperacionContext> requiereRevision() {
        return actorActivo.andSpec(autorizado.not().orSpec(detalleNoVacio.not()));
    }
}

