package com.turnoapp.backend.dto.appointment;

import com.turnoapp.backend.model.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar el estado de un turno.
 *
 * Usado por el profesional para cambiar el estado del turno:
 * - COMPLETED: Turno completado
 * - NO_SHOW: Cliente no asistió
 * - CANCELLED: Turno cancelado
 *
 * Patrón: Data Transfer Object (DTO)
 */
public record UpdateAppointmentStatusRequest(

        /**
         * Nuevo estado del turno
         */
        @NotNull(message = "El estado es obligatorio")
        AppointmentStatus status
) {
}
