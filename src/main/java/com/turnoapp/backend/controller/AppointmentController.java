package com.turnoapp.backend.controller;

import com.turnoapp.backend.config.security.CustomUserDetails;
import com.turnoapp.backend.dto.appointment.*;
import com.turnoapp.backend.model.enums.UserRole;
import com.turnoapp.backend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;


    @GetMapping
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<List<AppointmentResponse>> getAppointments(Authentication authentication) {
        Long professionalId = getProfessionalId(authentication);

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByProfessional(professionalId);

        return ResponseEntity.ok(appointments);
    }


    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<AppointmentResponse>> getClientAppointments(Authentication authentication) {
        Long clientId = getClientId(authentication);

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByClient(clientId);

        return ResponseEntity.ok(appointments);
    }


    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            Authentication authentication
    ) {
        Long clientId = getClientId(authentication);

        AppointmentResponse appointment = appointmentService.createAppointment(request, clientId);

        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'CLIENT')")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = getUserEntityId(userDetails);
        boolean isProfessional = userDetails.getRole() == UserRole.PROFESSIONAL;

        AppointmentResponse appointment = appointmentService.updateAppointmentStatus(
                id,
                request,
                userId,
                isProfessional
        );

        return ResponseEntity.ok(appointment);
    }


    @GetMapping("/availability/dates")
    public ResponseEntity<AvailabilityDateResponse> getAvailabilityByDates(
            @RequestParam Long professionalId,
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {

        // Validar rango de fechas
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La fecha final debe ser posterior a la fecha inicial");
        }

        // Limitar a máximo 60 días
        if (endDate.isAfter(startDate.plusDays(60))) {
            throw new IllegalArgumentException("El rango máximo es de 60 días");
        }

        AvailabilityDateResponse availability = appointmentService.getAvailabilityByDates(
                professionalId,
                serviceId,
                startDate,
                endDate
        );

        return ResponseEntity.ok(availability);
    }

    @GetMapping("/availability/slots")
    public ResponseEntity<AvailabilitySlotResponse> getAvailableSlots(
            @RequestParam Long professionalId,
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {

        AvailabilitySlotResponse slots = appointmentService.getAvailableSlots(
                professionalId,
                serviceId,
                date
        );

        return ResponseEntity.ok(slots);
    }

    // ==================== MÉTODOS AUXILIARES ====================


    private Long getProfessionalId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long professionalId = userDetails.getProfessionalId();

        if (professionalId == null) {
            throw new RuntimeException("Professional ID no encontrado en el token");
        }

        return professionalId;
    }


    private Long getClientId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        // En el contexto actual, el userId es el mismo que el clientId/professionalId
        // porque las relaciones son OneToOne con User
        return userId;
    }


    private Long getUserEntityId(CustomUserDetails userDetails) {
        if (userDetails.getRole() == UserRole.PROFESSIONAL) {
            Long professionalId = userDetails.getProfessionalId();
            if (professionalId == null) {
                throw new RuntimeException("Professional ID no encontrado en el token");
            }
            return professionalId;
        } else if (userDetails.getRole() == UserRole.CLIENT) {
            return userDetails.getUserId(); // Para clientes, usar userId
        } else {
            throw new RuntimeException("Rol no válido para esta operación");
        }
    }
}
