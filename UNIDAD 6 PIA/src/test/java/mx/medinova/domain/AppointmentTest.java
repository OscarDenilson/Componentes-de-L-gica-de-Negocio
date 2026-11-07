package mx.medinova.domain;
import org.junit.jupiter.api.Test;import java.time.*;import static org.junit.jupiter.api.Assertions.*;
class AppointmentTest { LocalDateTime start=LocalDateTime.of(2026,11,1,10,0); Appointment a(){return new Appointment(1L,2L,3L,"General",start,start.plusMinutes(30),"Consulta");}
 @Test void rejectsInvalidInterval(){assertThrows(IllegalArgumentException.class,()->new Appointment(1L,2L,3L,"General",start,start,"Consulta"));}
 @Test void latenessDoesNotAutoDecide(){var a=a();a.registerArrival(start.plusMinutes(20));assertEquals(20,a.getLatenessMinutes());assertEquals(AppointmentStatus.LLEGADA_REGISTRADA,a.getStatus());assertNull(a.getMedicalDecision());}
 @Test void assignedDoctorCanDecide(){var a=a();a.registerArrival(start.plusMinutes(5));a.decide(2L,MedicalDecision.ATENDER,"Evaluado",start.plusMinutes(6));assertEquals(AppointmentStatus.ATENDIDA,a.getStatus());}
 @Test void otherDoctorCannotDecide(){var a=a();a.registerArrival(start);assertThrows(SecurityException.class,()->a.decide(99L,MedicalDecision.ATENDER,"",start));}
 @Test void cannotDecideBeforeArrival(){assertThrows(IllegalStateException.class,()->a().decide(2L,MedicalDecision.ATENDER,"",start));}
 @Test void cancellationEmitsEvent(){var a=a();a.cancel("Paciente solicita");assertEquals(AppointmentStatus.CANCELADA,a.getStatus());assertEquals("CitaCancelada",a.pullEvents().get(0).type());}
 @Test void specificationsCombineAndNot(){var r=new AppointmentSpecifications.BookingRequest(1L,2L,3L,"General",start,start.plusMinutes(30),true,true,true,true,false);assertFalse(AppointmentSpecifications.canBook().test(r));assertTrue(AppointmentSpecifications.needsManualReview().test(r));}
 @Test void confirmationRequiresRequested(){var a=a();a.confirm();assertEquals(AppointmentStatus.CONFIRMADA,a.getStatus());assertThrows(IllegalStateException.class,a::confirm);}
 @Test void waitKeepsArrivalState(){var a=a();a.registerArrival(start);a.decide(2L,MedicalDecision.ESPERAR,"En espera",start);assertEquals(AppointmentStatus.LLEGADA_REGISTRADA,a.getStatus());}
 @Test void rescheduleChangesStatus(){var a=a();a.registerArrival(start);a.decide(2L,MedicalDecision.REPROGRAMAR,"Agenda llena",start);assertEquals(AppointmentStatus.REQUIERE_REPROGRAMACION,a.getStatus());}
 @Test void operacionIdBinding(){var a=new Appointment(1L,2L,3L,"General",start,start.plusMinutes(30),"Consulta","CITA-O001");assertEquals("CITA-O001",a.getOperacionId());a.setOperacionId("CITA-O002");assertEquals("CITA-O002",a.getOperacionId());}
}
