package com.turnoapp.backend.service;

import com.turnoapp.backend.dto.appointment.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface para gestión de turnos.
 *
 * Responsabilidades:
 * - Gestión CRUD de turnos
 * - Cálculo de disponibilidad (2 endpoints)
 * - Validaciones de negocio (solapamiento, horarios, permisos)
 *
 * Patrón: Service Layer Pattern
 * Principio: Dependency Inversion (depender de abstracciones, no implementaciones)
 * Principio: Interface Segregation (interfaz específica para turnos)
 */
public interface AppointmentService {

    /**
     * Obtiene todos los turnos de un profesional
     *
     * @param professionalId ID del profesional
     * @return Lista de turnos ordenados por fecha descendente
     */
    List<AppointmentResponse> getAppointmentsByProfessional(Long professionalId);

    /**
     * Obtiene todos los turnos de un cliente
     *
     * @param clientId ID del cliente
     * @return Lista de turnos ordenados por fecha descendente
     */
    List<AppointmentResponse> getAppointmentsByClient(Long clientId);

    /**
     * Obtiene un turno específico por ID
     *
     * @param id ID del turno
     * @param professionalId ID del profesional (para autorización)
     * @return Turno encontrado
     * @throws com.turnoapp.backend.exception.ResourceNotFoundException si no existe
     */
    AppointmentResponse getAppointmentById(Long id, Long professionalId);

    /**
     * Crea un nuevo turno (cliente agenda).
     *
     * Validaciones:
     * - Fecha debe ser futura
     * - No debe solaparse con otros turnos
     * - Debe estar dentro del horario de agenda configurado
     * - El servicio debe pertenecer al profesional
     *
     * @param request Datos del turno
     * @param clientId ID del cliente (extraído del JWT)
     * @return Turno creado con estado CONFIRMED
     * @throws IllegalArgumentException si hay validaciones fallidas
     */
    AppointmentResponse createAppointment(CreateAppointmentRequest request, Long clientId);

    /**
     * Actualiza el estado de un turno (profesional).
     *
     * Reglas:
     * - Solo turnos CONFIRMED pueden cambiar de estado
     * - COMPLETED / NO_SHOW: Solo profesional
     * - CANCELLED: Profesional o cliente
     *
     * @param id ID del turno
     * @param request Nuevo estado
     * @param userId ID del usuario (profesional o cliente)
     * @param isProfessional true si es profesional, false si es cliente
     * @return Turno actualizado
     * @throws IllegalStateException si el turno no puede ser modificado
     * @throws IllegalArgumentException si no tiene permisos
     */
    AppointmentResponse updateAppointmentStatus(
            Long id,
            UpdateAppointmentStatusRequest request,
            Long userId,
            boolean isProfessional
    );

    /**
     * Obtiene disponibilidad por rango de fechas (para calendario).
     *
     * Algoritmo:
     * 1. Query batch de turnos en el rango
     * 2. Para cada fecha, verificar si hay al menos 1 slot disponible
     * 3. Retornar lista con {date, hasAvailability}
     *
     * Performance: ~2KB para 30 días
     *
     * @param professionalId ID del profesional
     * @param serviceId ID del servicio
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Lista de fechas con disponibilidad
     */
    AvailabilityDateResponse getAvailabilityByDates(
            Long professionalId,
            Long serviceId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Obtiene slots disponibles para una fecha específica.
     *
     * Algoritmo (CRÍTICO):
     * 1. Obtener servicio (para duración)
     * 2. Obtener bloques de agenda para ese día de la semana
     * 3. Generar slots dinámicos según duración del servicio
     * 4. Obtener turnos existentes para esa fecha (excluir CANCELLED)
     * 5. Marcar slots como ocupados si se solapan con turnos
     *
     * Performance: ~500 bytes por día
     *
     * @param professionalId ID del profesional
     * @param serviceId ID del servicio
     * @param date Fecha a consultar
     * @return Lista de slots con disponibilidad
     */
    AvailabilitySlotResponse getAvailableSlots(
            Long professionalId,
            Long serviceId,
            LocalDate date
    );
}
