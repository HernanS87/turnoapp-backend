package com.turnoapp.backend.dto.appointment;

/**
 * DTO que representa un slot de tiempo con disponibilidad.
 *
 * Ejemplo:
 * - startTime: "09:00"
 * - endTime: "10:00"
 * - available: true (no hay turno agendado en ese horario)
 *
 * Patrón: Value Object
 */
public record TimeSlot(
        /**
         * Hora de inicio del slot (formato HH:mm)
         */
        String startTime,

        /**
         * Hora de fin del slot (formato HH:mm)
         */
        String endTime,

        /**
         * true si el slot está disponible (no hay turno agendado)
         */
        Boolean available
) {
}
