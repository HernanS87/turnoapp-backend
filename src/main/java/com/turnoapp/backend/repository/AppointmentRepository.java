package com.turnoapp.backend.repository;

import com.turnoapp.backend.model.Appointment;
import com.turnoapp.backend.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar turnos (Appointments).
 *
 * Proporciona métodos para:
 * - Filtrar turnos por profesional o cliente
 * - Verificar solapamientos de horarios
 * - Consultar disponibilidad por rango de fechas
 *
 * Patrón: Repository Pattern (abstracción de acceso a datos)
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Busca todos los turnos de un profesional, ordenados por fecha descendente
     *
     * @param professionalId ID del profesional
     * @return Lista de turnos ordenados por fecha y hora (más recientes primero)
     */
    List<Appointment> findByProfessionalIdOrderByDateDescStartTimeDesc(Long professionalId);

    /**
     * Busca todos los turnos de un cliente, ordenados por fecha descendente
     *
     * @param clientId ID del cliente
     * @return Lista de turnos ordenados por fecha y hora (más recientes primero)
     */
    List<Appointment> findByClientIdOrderByDateDescStartTimeDesc(Long clientId);

    /**
     * Busca un turno por ID y profesional (para autorización)
     *
     * @param id ID del turno
     * @param professionalId ID del profesional
     * @return Optional con el turno si pertenece al profesional
     */
    Optional<Appointment> findByIdAndProfessionalId(Long id, Long professionalId);

    /**
     * Busca un turno por ID y cliente (para autorización)
     *
     * @param id ID del turno
     * @param clientId ID del cliente
     * @return Optional con el turno si pertenece al cliente
     */
    Optional<Appointment> findByIdAndClientId(Long id, Long clientId);

    /**
     * Busca turnos de un profesional en un rango de fechas
     * Útil para calcular disponibilidad batch
     *
     * @param professionalId ID del profesional
     * @param startDate Fecha inicial (inclusive)
     * @param endDate Fecha final (inclusive)
     * @return Lista de turnos en el rango
     */
    List<Appointment> findByProfessionalIdAndDateBetween(
            Long professionalId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Busca turnos de un profesional en una fecha específica, excluyendo un estado
     * Útil para verificar disponibilidad (excluir CANCELLED)
     *
     * @param professionalId ID del profesional
     * @param date Fecha a consultar
     * @param excludeStatus Estado a excluir (típicamente CANCELLED)
     * @return Lista de turnos activos en esa fecha
     */
    List<Appointment> findByProfessionalIdAndDateAndStatusNot(
            Long professionalId,
            LocalDate date,
            AppointmentStatus excludeStatus
    );

    /**
     * Verifica si existen turnos que se solapen con el rango horario especificado.
     *
     * Lógica de solapamiento:
     * Dos rangos [A_start, A_end] y [B_start, B_end] se solapan si:
     *   A_start < B_end AND A_end > B_start
     *
     * Ejemplo:
     *   Turno existente: 09:00 - 10:00
     *   Nuevo turno: 09:30 - 10:30
     *   Check: 09:30 < 10:00 AND 10:30 > 09:00 → TRUE (se solapan)
     *
     * @param professionalId ID del profesional
     * @param date Fecha del turno
     * @param startTime Hora de inicio (HH:mm)
     * @param endTime Hora de fin (HH:mm)
     * @param excludeId ID del turno a excluir (usar -1 si no aplica)
     * @return Lista de turnos que se solapan (vacía si no hay solapamiento)
     */
    @Query("SELECT a FROM Appointment a WHERE a.professional.id = :professionalId " +
           "AND a.date = :date " +
           "AND a.status != 'CANCELLED' " +
           "AND a.id != :excludeId " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findOverlappingAppointments(
            @Param("professionalId") Long professionalId,
            @Param("date") LocalDate date,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("excludeId") Long excludeId
    );

    /**
     * Cuenta turnos de un profesional en una fecha específica con ciertos estados
     * Útil para estadísticas
     *
     * @param professionalId ID del profesional
     * @param date Fecha a consultar
     * @param status Estado del turno
     * @return Cantidad de turnos
     */
    long countByProfessionalIdAndDateAndStatus(
            Long professionalId,
            LocalDate date,
            AppointmentStatus status
    );

    /**
     * Verifica si un cliente tiene turnos activos con un profesional
     * Útil para validaciones de negocio
     *
     * @param clientId ID del cliente
     * @param professionalId ID del profesional
     * @param status Estado del turno
     * @return true si existe al menos un turno
     */
    boolean existsByClientIdAndProfessionalIdAndStatus(
            Long clientId,
            Long professionalId,
            AppointmentStatus status
    );
}
