package com.turnoapp.backend.dto.appointment;

import com.turnoapp.backend.model.Appointment;
import com.turnoapp.backend.model.enums.AppointmentStatus;

import java.time.Instant;

/**
 * DTO de respuesta para un turno.
 *
 * Incluye información enriquecida (nombres, detalles del servicio)
 * para evitar múltiples llamadas al API.
 *
 * Patrón: Data Transfer Object (DTO)
 * Patrón: Factory Method (método estático fromEntity)
 */
public record AppointmentResponse(
        Long id,
        Long professionalId,
        String professionalName,
        Long clientId,
        String clientName,
        String clientEmail,
        Long serviceId,
        String serviceName,
        Integer serviceDuration,
        String date,
        String startTime,
        String endTime,
        AppointmentStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Factory Method para crear un DTO desde una entidad.
     *
     * Patrón: Factory Method
     * Principio: Single Responsibility - La lógica de conversión está encapsulada
     *
     * @param appointment Entidad a convertir
     * @return DTO con datos enriquecidos
     */
    public static AppointmentResponse fromEntity(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getProfessional().getId(),
                appointment.getProfessional().getFullName(),
                appointment.getClient().getId(),
                appointment.getClient().getFullName(),
                appointment.getClient().getEmail(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getService().getDuration(),
                appointment.getDate().toString(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getNotes(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
