package com.turnoapp.backend.dto.appointment;

import java.util.List;

/**
 * DTO de respuesta para slots de tiempo disponibles en una fecha específica.
 *
 * Endpoint: GET /api/appointments/availability/slots
 * Propósito: Obtener horarios disponibles para un día específico
 * Uso: Mostrar selector de horarios en la página de reserva
 *
 * Algoritmo:
 * 1. Obtener bloques de agenda del profesional para ese día de la semana
 * 2. Generar slots dinámicos según duración del servicio
 * 3. Marcar slots como ocupados si hay turnos agendados
 *
 * Patrón: Data Transfer Object (DTO)
 * Performance: Respuesta liviana (~500 bytes por día)
 */
public record AvailabilitySlotResponse(
        Long professionalId,
        Long serviceId,
        String date,
        Integer serviceDuration,
        List<TimeSlot> slots
) {
}
