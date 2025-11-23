package com.turnoapp.backend.model.enums;

/**
 * Estados posibles de un turno en el sistema.
 *
 * Ciclo de vida:
 * - CONFIRMED: Estado inicial cuando el cliente agenda (auto-confirmado)
 * - COMPLETED: El profesional marca el turno como completado
 * - NO_SHOW: El cliente no asistió (solo profesional puede marcar)
 * - CANCELLED: Cancelado por cliente o profesional
 */
public enum AppointmentStatus {
    /**
     * Turno confirmado (estado inicial al agendar)
     */
    CONFIRMED,

    /**
     * Turno completado exitosamente
     */
    COMPLETED,

    /**
     * Cliente no asistió al turno
     */
    NO_SHOW,

    /**
     * Turno cancelado
     */
    CANCELLED
}
