package com.turnoapp.backend.model;

import com.turnoapp.backend.model.enums.AppointmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Appointment.
 * 
 * Enfoque: Testing de lógica de negocio pura sin dependencias externas.
 * Demuestra: TDD, pruebas unitarias simples y claras.
 */
@DisplayName("Appointment - Pruebas de Lógica de Negocio")
class AppointmentTest {

    @Test
    @DisplayName("Un turno CONFIRMED debe ser modificable")
    void testConfirmedAppointmentIsModifiable() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.CONFIRMED)
                .build();

        // Act
        boolean isModifiable = appointment.isModifiable();

        // Assert
        assertTrue(isModifiable, "Un turno CONFIRMED debe ser modificable");
    }

    @Test
    @DisplayName("Un turno CANCELLED no debe ser modificable")
    void testCancelledAppointmentIsNotModifiable() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.CANCELLED)
                .build();

        // Act
        boolean isModifiable = appointment.isModifiable();

        // Assert
        assertFalse(isModifiable, "Un turno CANCELLED no debe ser modificable");
    }

    @Test
    @DisplayName("Un turno COMPLETED no debe ser modificable")
    void testCompletedAppointmentIsNotModifiable() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.COMPLETED)
                .build();

        // Act
        boolean isModifiable = appointment.isModifiable();

        // Assert
        assertFalse(isModifiable, "Un turno COMPLETED no debe ser modificable");
    }

    @Test
    @DisplayName("Un turno NO_SHOW no debe ser modificable")
    void testNoShowAppointmentIsNotModifiable() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.NO_SHOW)
                .build();

        // Act
        boolean isModifiable = appointment.isModifiable();

        // Assert
        assertFalse(isModifiable, "Un turno NO_SHOW no debe ser modificable");
    }

    @Test
    @DisplayName("Un turno CANCELLED no debe estar activo")
    void testCancelledAppointmentIsNotActive() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.CANCELLED)
                .build();

        // Act
        boolean isActive = appointment.isActive();

        // Assert
        assertFalse(isActive, "Un turno CANCELLED no debe estar activo");
    }

    @Test
    @DisplayName("Un turno CONFIRMED debe estar activo")
    void testConfirmedAppointmentIsActive() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.CONFIRMED)
                .build();

        // Act
        boolean isActive = appointment.isActive();

        // Assert
        assertTrue(isActive, "Un turno CONFIRMED debe estar activo");
    }

    @Test
    @DisplayName("Un turno COMPLETED debe estar finalizado")
    void testCompletedAppointmentIsFinished() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.COMPLETED)
                .build();

        // Act
        boolean isFinished = appointment.isFinished();

        // Assert
        assertTrue(isFinished, "Un turno COMPLETED debe estar finalizado");
    }

    @Test
    @DisplayName("Un turno NO_SHOW debe estar finalizado")
    void testNoShowAppointmentIsFinished() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.NO_SHOW)
                .build();

        // Act
        boolean isFinished = appointment.isFinished();

        // Assert
        assertTrue(isFinished, "Un turno NO_SHOW debe estar finalizado");
    }
}

