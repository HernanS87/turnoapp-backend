package com.turnoapp.backend.model;

import com.turnoapp.backend.model.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad que representa un turno agendado en el sistema.
 *
 * Relaciones:
 * - Un profesional puede tener muchos turnos (ManyToOne)
 * - Un cliente puede tener muchos turnos (ManyToOne)
 * - Un turno está asociado a un servicio específico (ManyToOne)
 *
 * Validaciones:
 * - Fecha debe ser futura
 * - No puede solaparse con otros turnos del mismo profesional
 * - Debe estar dentro del horario de agenda configurado
 * - Formato de hora: HH:mm (ej: 09:30, 14:00)
 */
@Entity
@Table(
    name = "appointments",
    indexes = {
        @Index(name = "idx_professional_date", columnList = "professional_id, date"),
        @Index(name = "idx_client_date", columnList = "client_id, date"),
        @Index(name = "idx_date_time", columnList = "date, start_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Profesional que brinda el servicio
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    @NotNull(message = "El profesional es obligatorio")
    private Professional professional;

    /**
     * Cliente que agenda el turno
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    private Client client;

    /**
     * Servicio asociado al turno
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @NotNull(message = "El servicio es obligatorio")
    private Service service;

    /**
     * Fecha del turno (formato: yyyy-MM-dd)
     */
    @Column(nullable = false)
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    /**
     * Hora de inicio (formato: HH:mm, ej: 09:00, 14:30)
     */
    @Column(name = "start_time", nullable = false, length = 5)
    @NotNull(message = "La hora de inicio es obligatoria")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Formato de hora inválido (debe ser HH:mm)")
    private String startTime;

    /**
     * Hora de fin (formato: HH:mm)
     * Se calcula automáticamente: startTime + service.duration
     */
    @Column(name = "end_time", nullable = false, length = 5)
    @NotNull(message = "La hora de fin es obligatoria")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Formato de hora inválido (debe ser HH:mm)")
    private String endTime;

    /**
     * Estado del turno
     * Default: CONFIRMED (auto-confirmado al agendar)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.CONFIRMED;

    /**
     * Notas adicionales del cliente (opcional)
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Fecha de creación (auto-generada)
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Fecha de última actualización (auto-generada)
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    // Business methods

    /**
     * Verifica si el turno puede ser modificado
     * Solo turnos CONFIRMED pueden cambiar de estado
     */
    public boolean isModifiable() {
        return status == AppointmentStatus.CONFIRMED;
    }

    /**
     * Verifica si el turno está activo (no cancelado)
     */
    public boolean isActive() {
        return status != AppointmentStatus.CANCELLED;
    }

    /**
     * Verifica si el turno está finalizado (completado o ausente)
     */
    public boolean isFinished() {
        return status == AppointmentStatus.COMPLETED || status == AppointmentStatus.NO_SHOW;
    }
}
