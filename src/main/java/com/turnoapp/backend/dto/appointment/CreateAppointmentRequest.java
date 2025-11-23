package com.turnoapp.backend.dto.appointment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear un nuevo turno.
 *
 * Usado por el cliente cuando agenda un turno.
 * El profesionalId y clientId se extraen del JWT.
 *
 * Patrón: Data Transfer Object (DTO)
 * Principio: Interface Segregation (ISP) - Solo campos necesarios para creación
 */
public record CreateAppointmentRequest(

        /**
         * ID del servicio a agendar
         */
        @NotNull(message = "El servicio es obligatorio")
        Long serviceId,

        /**
         * Fecha del turno (formato: yyyy-MM-dd)
         */
        @NotNull(message = "La fecha es obligatoria")
        @Pattern(
                regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "Formato de fecha inválido (debe ser yyyy-MM-dd)"
        )
        String date,

        /**
         * Hora de inicio (formato: HH:mm, ej: 09:00, 14:30)
         */
        @NotNull(message = "La hora de inicio es obligatoria")
        @Pattern(
                regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
                message = "Formato de hora inválido (debe ser HH:mm)"
        )
        String startTime,

        /**
         * Notas adicionales del cliente (opcional)
         */
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        String notes
) {
}
