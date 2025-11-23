package com.turnoapp.backend.dto.appointment;

/**
 * DTO que representa la disponibilidad de una fecha específica.
 *
 * Usado en el calendario para marcar qué días tienen horarios disponibles.
 *
 * Patrón: Value Object
 */
public record DateAvailability(
        /**
         * Fecha en formato yyyy-MM-dd
         */
        String date,

        /**
         * true si hay al menos un horario disponible en esta fecha
         */
        Boolean hasAvailability
) {
}
