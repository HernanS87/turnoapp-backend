package com.turnoapp.backend.service.impl;

import com.turnoapp.backend.dto.appointment.*;
import com.turnoapp.backend.exception.ResourceNotFoundException;
import com.turnoapp.backend.model.*;
import com.turnoapp.backend.model.enums.AppointmentStatus;
import com.turnoapp.backend.repository.*;
import com.turnoapp.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de turnos.
 *
 * Principios aplicados:
 * - Single Responsibility: Cada método tiene una responsabilidad clara
 * - Dependency Inversion: Depende de interfaces (repositories)
 * - Open/Closed: Extensible sin modificar código existente
 *
 * Patrones:
 * - Service Layer Pattern
 * - Repository Pattern (delegación a repositories)
 * - Strategy Pattern (validaciones intercambiables)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final ScheduleRepository scheduleRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByProfessional(Long professionalId) {
        log.debug("Obteniendo turnos del profesional: {}", professionalId);

        List<Appointment> appointments = appointmentRepository
                .findByProfessionalIdOrderByDateDescStartTimeDesc(professionalId);

        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByClient(Long clientId) {
        log.debug("Obteniendo turnos del cliente: {}", clientId);

        List<Appointment> appointments = appointmentRepository
                .findByClientIdOrderByDateDescStartTimeDesc(clientId);

        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id, Long professionalId) {
        log.debug("Obteniendo turno: {} del profesional: {}", id, professionalId);

        Appointment appointment = appointmentRepository
                .findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        return AppointmentResponse.fromEntity(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, Long clientId) {
        log.info("Creando turno para cliente: {} - Servicio: {} - Fecha: {} {}",
                clientId, request.serviceId(), request.date(), request.startTime());

        // 1. Validar que el cliente existe
        Client client = clientRepository.findByUserId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // 2. Validar que el servicio existe
        var service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        Professional professional = service.getProfessional();

        // 3. Parsear fecha
        LocalDate date = LocalDate.parse(request.date());

        // 4. Validar que la fecha sea futura
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden agendar turnos en fechas pasadas");
        }

        // 5. Calcular hora de fin (startTime + duration)
        LocalTime start = LocalTime.parse(request.startTime(), TIME_FORMATTER);
        LocalTime end = start.plusMinutes(service.getDuration());
        String endTime = end.format(TIME_FORMATTER);

        // 6. Validar que esté dentro del horario de agenda
        validateWithinSchedule(professional.getId(), date, request.startTime(), endTime);

        // 7. Validar que no se solape con otros turnos
        validateNoOverlap(professional.getId(), date, request.startTime(), endTime, null);

        // 8. Crear turno (estado inicial: CONFIRMED)
        Appointment appointment = Appointment.builder()
                .professional(professional)
                .client(client)
                .service(service)
                .date(date)
                .startTime(request.startTime())
                .endTime(endTime)
                .status(AppointmentStatus.CONFIRMED) // Auto-confirmado
                .notes(request.notes())
                .build();

        appointment = appointmentRepository.save(appointment);

        log.info("Turno creado exitosamente: ID={}", appointment.getId());
        return AppointmentResponse.fromEntity(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointmentStatus(
            Long id,
            UpdateAppointmentStatusRequest request,
            Long userId,
            boolean isProfessional
    ) {
        log.info("Actualizando estado del turno: {} a {} - Usuario: {} (isProfessional: {})",
                id, request.status(), userId, isProfessional);

        // 1. Buscar turno
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        // 2. Validar que solo turnos CONFIRMED pueden cambiar de estado
        if (!appointment.isModifiable()) {
            throw new IllegalStateException("Solo turnos confirmados pueden cambiar de estado");
        }

        // 3. Validar permisos según el nuevo estado
        AppointmentStatus newStatus = request.status();

        switch (newStatus) {
            case COMPLETED, NO_SHOW -> {
                // Solo el profesional puede marcar como completado o ausente
                if (!isProfessional) {
                    throw new IllegalArgumentException("Solo el profesional puede cambiar a este estado");
                }
                if (!appointment.getProfessional().getId().equals(userId)) {
                    throw new IllegalArgumentException("No tienes permiso para modificar este turno");
                }
            }
            case CANCELLED -> {
                // Ambos pueden cancelar
                boolean isOwnerProfessional = appointment.getProfessional().getId().equals(userId) && isProfessional;
                boolean isOwnerClient = appointment.getClient().getId().equals(userId) && !isProfessional;

                if (!isOwnerProfessional && !isOwnerClient) {
                    throw new IllegalArgumentException("No tienes permiso para cancelar este turno");
                }
            }
            case CONFIRMED -> {
                throw new IllegalArgumentException("No se puede volver a estado CONFIRMED");
            }
        }

        // 4. Actualizar estado
        appointment.setStatus(newStatus);
        appointment = appointmentRepository.save(appointment);

        log.info("Estado del turno actualizado exitosamente: ID={}, Estado={}", id, newStatus);
        return AppointmentResponse.fromEntity(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityDateResponse getAvailabilityByDates(
            Long professionalId,
            Long serviceId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.debug("Calculando disponibilidad por fechas - Profesional: {}, Servicio: {}, Rango: {} a {}",
                professionalId, serviceId, startDate, endDate);

        // 1. Validar que el servicio existe y pertenece al profesional
        com.turnoapp.backend.model.Service service = serviceRepository
                .findByIdAndProfessionalId(serviceId, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // 2. Obtener turnos existentes en el rango (batch query optimizado)
        List<Appointment> existingAppointments = appointmentRepository
                .findByProfessionalIdAndDateBetween(professionalId, startDate, endDate);

        // 3. Obtener agenda completa del profesional (todos los días de la semana)
        List<ScheduleSlot> scheduleSlots = scheduleRepository
                .findByProfessionalIdAndActiveTrue(professionalId);

        if (scheduleSlots.isEmpty()) {
            log.warn("Profesional {} no tiene agenda configurada", professionalId);
            return new AvailabilityDateResponse(professionalId, serviceId, List.of());
        }

        // 4. Iterar cada fecha del rango y verificar disponibilidad
        List<DateAvailability> availability = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            boolean hasAvailability = hasAvailabilityForDate(
                    currentDate,
                    service.getDuration(),
                    scheduleSlots,
                    existingAppointments
            );

            availability.add(new DateAvailability(currentDate.toString(), hasAvailability));
            currentDate = currentDate.plusDays(1);
        }

        log.debug("Disponibilidad calculada: {} fechas procesadas", availability.size());
        return new AvailabilityDateResponse(professionalId, serviceId, availability);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilitySlotResponse getAvailableSlots(
            Long professionalId,
            Long serviceId,
            LocalDate date
    ) {
        log.debug("Calculando slots disponibles - Profesional: {}, Servicio: {}, Fecha: {}",
                professionalId, serviceId, date);

        // 1. Validar que el servicio existe y pertenece al profesional
        com.turnoapp.backend.model.Service service = serviceRepository
                .findByIdAndProfessionalId(serviceId, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // 2. Convertir fecha a día de la semana (0 = Domingo, 6 = Sábado)
        int dayOfWeek = convertToDayOfWeek(date.getDayOfWeek());

        // 3. Obtener bloques de agenda para ese día
        List<ScheduleSlot> scheduleSlots = scheduleRepository
                .findByProfessionalIdAndDayOfWeekAndActiveTrue(professionalId, dayOfWeek);

        if (scheduleSlots.isEmpty()) {
            log.warn("No hay agenda configurada para el día {} (dayOfWeek={})", date, dayOfWeek);
            return new AvailabilitySlotResponse(professionalId, serviceId, date.toString(),
                    service.getDuration(), List.of());
        }

        // 4. Obtener turnos existentes para esa fecha (excluir CANCELLED)
        List<Appointment> existingAppointments = appointmentRepository
                .findByProfessionalIdAndDateAndStatusNot(professionalId, date, AppointmentStatus.CANCELLED);

        // 5. Generar slots dinámicos
        List<TimeSlot> timeSlots = generateTimeSlots(
                scheduleSlots,
                service.getDuration(),
                existingAppointments
        );

        log.debug("Slots generados: {} para fecha {}", timeSlots.size(), date);
        return new AvailabilitySlotResponse(
                professionalId,
                serviceId,
                date.toString(),
                service.getDuration(),
                timeSlots
        );
    }

    // ==================== MÉTODOS PRIVADOS DE VALIDACIÓN ====================

    /**
     * Valida que el turno esté dentro del horario de agenda configurado.
     */
    private void validateWithinSchedule(Long professionalId, LocalDate date, String startTime, String endTime) {
        int dayOfWeek = convertToDayOfWeek(date.getDayOfWeek());

        List<ScheduleSlot> scheduleSlots = scheduleRepository
                .findByProfessionalIdAndDayOfWeekAndActiveTrue(professionalId, dayOfWeek);

        if (scheduleSlots.isEmpty()) {
            throw new IllegalArgumentException("No hay agenda configurada para este día");
        }

        // Verificar que el rango [startTime, endTime] esté contenido en algún bloque de agenda
        boolean withinSchedule = scheduleSlots.stream().anyMatch(slot ->
                startTime.compareTo(slot.getStartTime()) >= 0 &&
                        endTime.compareTo(slot.getEndTime()) <= 0
        );

        if (!withinSchedule) {
            throw new IllegalArgumentException("El horario solicitado está fuera de la agenda disponible");
        }
    }

    /**
     * Valida que no haya solapamiento con otros turnos.
     */
    private void validateNoOverlap(Long professionalId, LocalDate date, String startTime, String endTime, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;

        List<Appointment> overlapping = appointmentRepository.findOverlappingAppointments(
                professionalId,
                date,
                startTime,
                endTime,
                excludeIdSafe
        );

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("El horario se solapa con un turno existente");
        }
    }

    // ==================== ALGORITMOS DE DISPONIBILIDAD ====================

    /**
     * Verifica si una fecha tiene al menos un slot disponible.
     *
     * Algoritmo:
     * 1. Verificar si hay agenda para ese día de la semana
     * 2. Generar slots potenciales según duración del servicio
     * 3. Verificar si al menos uno no se solapa con turnos existentes
     */
    private boolean hasAvailabilityForDate(
            LocalDate date,
            Integer serviceDuration,
            List<ScheduleSlot> allScheduleSlots,
            List<Appointment> existingAppointments
    ) {
        int dayOfWeek = convertToDayOfWeek(date.getDayOfWeek());

        // Filtrar agenda para este día
        List<ScheduleSlot> daySchedule = allScheduleSlots.stream()
                .filter(slot -> slot.getDayOfWeek().equals(dayOfWeek))
                .toList();

        if (daySchedule.isEmpty()) {
            return false;
        }

        // Filtrar turnos para esta fecha (excluir CANCELLED)
        List<Appointment> dayAppointments = existingAppointments.stream()
                .filter(apt -> apt.getDate().equals(date) && apt.getStatus() != AppointmentStatus.CANCELLED)
                .toList();

        // Verificar si hay al menos un slot disponible
        for (ScheduleSlot slot : daySchedule) {
            LocalTime currentTime = LocalTime.parse(slot.getStartTime(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(slot.getEndTime(), TIME_FORMATTER);

            while (currentTime.plusMinutes(serviceDuration).isBefore(endTime) ||
                    currentTime.plusMinutes(serviceDuration).equals(endTime)) {

                String slotStart = currentTime.format(TIME_FORMATTER);
                String slotEnd = currentTime.plusMinutes(serviceDuration).format(TIME_FORMATTER);

                // Verificar si este slot NO se solapa con ningún turno
                boolean available = dayAppointments.stream().noneMatch(apt ->
                        isOverlapping(slotStart, slotEnd, apt.getStartTime(), apt.getEndTime())
                );

                if (available) {
                    return true; // Encontramos al menos un slot disponible
                }

                currentTime = currentTime.plusMinutes(serviceDuration);
            }
        }

        return false; // No hay slots disponibles
    }

    /**
     * Genera todos los slots de tiempo para los bloques de agenda.
     *
     * Algoritmo CRÍTICO:
     * 1. Para cada bloque de agenda (ej: 09:00 - 13:00)
     * 2. Generar slots según duración del servicio (ej: 60min → 09:00-10:00, 10:00-11:00, etc.)
     * 3. Marcar slot como disponible si NO se solapa con turnos existentes
     */
    private List<TimeSlot> generateTimeSlots(
            List<ScheduleSlot> scheduleSlots,
            Integer serviceDuration,
            List<Appointment> existingAppointments
    ) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        for (ScheduleSlot scheduleSlot : scheduleSlots) {
            LocalTime currentTime = LocalTime.parse(scheduleSlot.getStartTime(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(scheduleSlot.getEndTime(), TIME_FORMATTER);

            // Generar slots mientras quepan en el bloque
            while (currentTime.plusMinutes(serviceDuration).isBefore(endTime) ||
                    currentTime.plusMinutes(serviceDuration).equals(endTime)) {

                String slotStart = currentTime.format(TIME_FORMATTER);
                String slotEnd = currentTime.plusMinutes(serviceDuration).format(TIME_FORMATTER);

                // Verificar si el slot está disponible (no se solapa con turnos)
                boolean available = existingAppointments.stream().noneMatch(apt ->
                        isOverlapping(slotStart, slotEnd, apt.getStartTime(), apt.getEndTime())
                );

                timeSlots.add(new TimeSlot(slotStart, slotEnd, available));

                // Avanzar al siguiente slot
                currentTime = currentTime.plusMinutes(serviceDuration);
            }
        }

        return timeSlots;
    }

    /**
     * Verifica si dos rangos horarios se solapan.
     *
     * Lógica: Dos rangos [A_start, A_end] y [B_start, B_end] se solapan si:
     *   A_start < B_end AND A_end > B_start
     *
     * Ejemplo:
     *   A: 09:00 - 10:00
     *   B: 09:30 - 10:30
     *   Check: 09:00 < 10:30 AND 10:00 > 09:30 → TRUE (se solapan)
     */
    private boolean isOverlapping(String start1, String end1, String start2, String end2) {
        return start1.compareTo(end2) < 0 && end1.compareTo(start2) > 0;
    }

    /**
     * Convierte DayOfWeek de Java a formato 0-6 (0=Domingo, 6=Sábado).
     */
    private int convertToDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue() % 7; // MONDAY=1 → 1, SUNDAY=7 → 0
    }
}
