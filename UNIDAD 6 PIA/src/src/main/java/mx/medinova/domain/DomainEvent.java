package mx.medinova.domain;
import java.time.Instant;
public record DomainEvent(String type, Long appointmentId, Instant occurredAt, String detail) {}
