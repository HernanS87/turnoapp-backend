package com.turnoapp.backend.dto.appointment;

import java.util.List;

/**
 * DTO de respuesta para disponibilidad por fechas (vista calendario).
 *
 * Endpoint: GET /api/appointments/availability/dates
 * Propósito: Obtener un rango de fechas con indicador booleano de disponibilidad
 * Uso: Marcar días disponibles en el calendario del frontend
 *
 * Patrón: Data Transfer Object (DTO)
 * Performance: Respuesta liviana (~2KB para 30 días)
 */
public record AvailabilityDateResponse(
        Long professionalId,
        Long serviceId,
        List<DateAvailability> availability
) {
}
