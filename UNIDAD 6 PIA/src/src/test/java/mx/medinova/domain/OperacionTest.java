package mx.medinova.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperacionTest {

    @Test
    void operacionLifeCycleAprobarYCompletar() {
        Operacion op = new Operacion("CITA-O001", "CITA-C001", LocalDate.now(), false, true, "REF1");
        assertEquals(OperacionStatus.SOLICITADA, op.getEstado());

        op.aprobar();
        assertEquals(OperacionStatus.CONFIRMADA, op.getEstado());
        List<DomainEvent> events1 = op.pullEvents();
        assertEquals(1, events1.size());
        assertEquals("OperacionConfirmada", events1.get(0).type());

        op.completar();
        assertEquals(OperacionStatus.ATENDIDA, op.getEstado());
        List<DomainEvent> events2 = op.pullEvents();
        assertEquals(1, events2.size());
        assertEquals("OperacionAtendida", events2.get(0).type());
    }

    @Test
    void cannotCompletarDirectlyFromSolicitada() {
        Operacion op = new Operacion("CITA-O002", "CITA-C002", LocalDate.now(), false, true, "REF2");
        assertThrows(IllegalStateException.class, op::completar);
    }

    @Test
    void cannotAprobarTwice() {
        Operacion op = new Operacion("CITA-O003", "CITA-C003", LocalDate.now(), false, true, "REF3");
        op.aprobar();
        assertThrows(IllegalStateException.class, op::aprobar);
    }

    @Test
    void cancelarOperacion() {
        Operacion op = new Operacion("CITA-O004", "CITA-C004", LocalDate.now(), false, true, "REF4");
        op.cancelar("Prueba de cancelación");
        assertEquals(OperacionStatus.CANCELADA, op.getEstado());

        assertThrows(IllegalStateException.class, () -> op.cancelar("Otra vez"));
    }

    @Test
    void detalleYTotalCalculations() {
        Operacion op = new Operacion("CITA-O005", "CITA-C005", LocalDate.now(), false, true, "REF5");
        DetalleOperacion d1 = new DetalleOperacion("CITA-O005", "CITA-P001", 2, new BigDecimal("250.00"));
        DetalleOperacion d2 = new DetalleOperacion("CITA-O005", "CITA-P002", 1, new BigDecimal("600.00"));

        assertEquals(new BigDecimal("500.00"), d1.getSubtotalMxn());
        assertEquals(new BigDecimal("600.00"), d2.getSubtotalMxn());

        op.agregarLinea(d1);
        op.agregarLinea(d2);

        assertEquals(new BigDecimal("1100.00"), op.getTotalMxn());
        assertEquals(2, op.getLineas().size());
    }

    @Test
    void specificationsPuedeProcesarse() {
        Actor actorActivo = new Actor("CITA-C001", "Juan", false, true, "Monterrey");
        Actor actorInactivo = new Actor("CITA-C002", "Pedro", false, false, "Apodaca");

        Operacion opValida = new Operacion("CITA-O001", "CITA-C001", LocalDate.now(), false, true, "REF");
        opValida.agregarLinea(new DetalleOperacion("CITA-O001", "CITA-P001", 1, new BigDecimal("300.00")));

        OperacionContext ctxValido = new OperacionContext(opValida, actorActivo);
        assertTrue(OperacionSpecifications.puedeProcesarse().test(ctxValido));

        OperacionContext ctxInactivo = new OperacionContext(opValida, actorInactivo);
        assertFalse(OperacionSpecifications.puedeProcesarse().test(ctxInactivo));

        Operacion opSinLineas = new Operacion("CITA-O002", "CITA-C001", LocalDate.now(), false, true, "REF");
        assertFalse(OperacionSpecifications.puedeProcesarse().test(new OperacionContext(opSinLineas, actorActivo)));

        Operacion opNoAutorizada = new Operacion("CITA-O003", "CITA-C001", LocalDate.now(), false, false, "REF");
        opNoAutorizada.agregarLinea(new DetalleOperacion("CITA-O003", "CITA-P001", 1, new BigDecimal("300.00")));
        assertFalse(OperacionSpecifications.puedeProcesarse().test(new OperacionContext(opNoAutorizada, actorActivo)));
    }

    @Test
    void specificationsBeneficioReferencia() {
        Actor vip = new Actor("CITA-C005", "VIP", true, true, "Monterrey");
        Actor regular = new Actor("CITA-C006", "Regular", false, true, "Monterrey");

        Operacion opMayor1500SinPromo = new Operacion("CITA-O001", "CITA-C005", LocalDate.now(), false, true, "REF");
        opMayor1500SinPromo.agregarLinea(new DetalleOperacion("CITA-O001", "CITA-P001", 3, new BigDecimal("600.00"))); // total 1800

        assertTrue(OperacionSpecifications.beneficioReferencia().test(new OperacionContext(opMayor1500SinPromo, vip)));
        assertFalse(OperacionSpecifications.beneficioReferencia().test(new OperacionContext(opMayor1500SinPromo, regular)));

        Operacion opConPromo = new Operacion("CITA-O002", "CITA-C005", LocalDate.now(), true, true, "REF");
        opConPromo.agregarLinea(new DetalleOperacion("CITA-O002", "CITA-P001", 3, new BigDecimal("600.00")));
        assertFalse(OperacionSpecifications.beneficioReferencia().test(new OperacionContext(opConPromo, vip)));

        Operacion opMenor1500 = new Operacion("CITA-O003", "CITA-C005", LocalDate.now(), false, true, "REF");
        opMenor1500.agregarLinea(new DetalleOperacion("CITA-O003", "CITA-P001", 1, new BigDecimal("500.00")));
        assertFalse(OperacionSpecifications.beneficioReferencia().test(new OperacionContext(opMenor1500, vip)));
    }

    @Test
    void testSetAutorizado() {
        Operacion op = new Operacion("CITA-O010", "CITA-C001", LocalDate.now(), false, false, "REF");
        assertFalse(op.isAutorizado());
        op.setAutorizado(true);
        assertTrue(op.isAutorizado());
        op.setAutorizado(false);
        assertFalse(op.isAutorizado());
    }
}

