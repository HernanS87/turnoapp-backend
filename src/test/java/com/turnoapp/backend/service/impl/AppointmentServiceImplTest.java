package com.turnoapp.backend.service.impl;

import com.turnoapp.backend.dto.appointment.CreateAppointmentRequest;
import com.turnoapp.backend.exception.ResourceNotFoundException;
import com.turnoapp.backend.model.*;
import com.turnoapp.backend.model.enums.AppointmentStatus;
import com.turnoapp.backend.model.enums.Status;
import com.turnoapp.backend.model.enums.UserRole;
import com.turnoapp.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AppointmentServiceImpl.
 * 
 * Enfoque: Testing con mocks para aislar la lógica de negocio.
 * Demuestra: Mocking, validaciones de negocio, casos de error.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentServiceImpl - Pruebas de Validación de Negocio")
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User professionalUser;
    private Professional professional;
    private User clientUser;
    private Client client;
    private Service service;
    private ScheduleSlot scheduleSlot;

    @BeforeEach
    void setUp() {
        // Setup Professional
        professionalUser = User.builder()
                .id(1L)
                .email("prof@test.com")
                .firstName("Profesional")
                .lastName("Test")
                .role(UserRole.PROFESSIONAL)
                .status(Status.ACTIVE)
                .build();

        professional = Professional.builder()
                .id(1L)
                .user(professionalUser)
                .profession("Psicólogo")
                .customUrl("prof-test")
                .build();

        // Setup Client
        clientUser = User.builder()
                .id(2L)
                .email("client@test.com")
                .firstName("Cliente")
                .lastName("Test")
                .role(UserRole.CLIENT)
                .status(Status.ACTIVE)
                .build();

        client = Client.builder()
                .id(1L)
                .user(clientUser)
                .build();

        // Setup Service
        service = Service.builder()
                .id(1L)
                .professional(professional)
                .name("Consulta")
                .description("Consulta psicológica")
                .price(5000.0)
                .duration(60) // 60 minutos
                .status(Status.ACTIVE)
                .build();

        // Setup Schedule
        scheduleSlot = ScheduleSlot.builder()
                .id(1L)
                .professional(professional)
                .dayOfWeek(1) // Lunes
                .startTime("09:00")
                .endTime("18:00")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando hay solapamiento de turnos")
    void testCreateAppointment_ThrowsException_WhenOverlapping() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String startTime = "10:00";
        String endTime = "11:00";

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L,
                tomorrow.toString(),
                startTime,
                "Notas de prueba"
        );

        // Mock existing appointment that overlaps
        Appointment existingAppointment = Appointment.builder()
                .id(1L)
                .professional(professional)
                .client(client)
                .service(service)
                .date(tomorrow)
                .startTime("10:30")
                .endTime("11:30")
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(clientRepository.findByUserId(2L)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(scheduleRepository.findByProfessionalIdAndDayOfWeekAndActiveTrue(1L, 1))
                .thenReturn(List.of(scheduleSlot));
        when(appointmentRepository.findOverlappingAppointments(
                eq(1L),
                eq(tomorrow),
                eq(startTime),
                eq(endTime),
                anyLong()
        )).thenReturn(List.of(existingAppointment));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(request, 2L),
                "Debe lanzar excepción cuando hay solapamiento"
        );

        assertEquals("El horario se solapa con un turno existente", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe permitir crear turnos consecutivos sin solapamiento")
    void testCreateAppointment_Success_WhenConsecutiveAppointments() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String startTime = "10:00";
        String endTime = "11:00";

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L,
                tomorrow.toString(),
                startTime,
                "Notas de prueba"
        );

        // Mock existing appointment that doesn't overlap (ends before new one starts)
        Appointment existingAppointment = Appointment.builder()
                .id(1L)
                .professional(professional)
                .client(client)
                .service(service)
                .date(tomorrow)
                .startTime("09:00")
                .endTime("10:00") // Ends exactly when new one starts
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(clientRepository.findByUserId(2L)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(scheduleRepository.findByProfessionalIdAndDayOfWeekAndActiveTrue(1L, 1))
                .thenReturn(List.of(scheduleSlot));
        when(appointmentRepository.findOverlappingAppointments(
                eq(1L),
                eq(tomorrow),
                eq(startTime),
                eq(endTime),
                anyLong()
        )).thenReturn(new ArrayList<>()); // No overlapping appointments

        Appointment savedAppointment = Appointment.builder()
                .id(2L)
                .professional(professional)
                .client(client)
                .service(service)
                .date(tomorrow)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.CONFIRMED)
                .notes("Notas de prueba")
                .build();

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        // Act
        var result = appointmentService.createAppointment(request, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(AppointmentStatus.CONFIRMED, result.status());
        assertEquals(startTime, result.startTime());
        assertEquals(endTime, result.endTime());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Debe permitir crear turnos en diferentes días")
    void testCreateAppointment_Success_WhenDifferentDays() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfter = LocalDate.now().plusDays(2);
        String startTime = "10:00";
        String endTime = "11:00";

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L,
                dayAfter.toString(),
                startTime,
                null
        );

        // Mock existing appointment on different day
        Appointment existingAppointment = Appointment.builder()
                .id(1L)
                .professional(professional)
                .client(client)
                .service(service)
                .date(tomorrow) // Different day
                .startTime("10:00")
                .endTime("11:00")
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(clientRepository.findByUserId(2L)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(scheduleRepository.findByProfessionalIdAndDayOfWeekAndActiveTrue(1L, 2))
                .thenReturn(List.of(scheduleSlot));
        when(appointmentRepository.findOverlappingAppointments(
                eq(1L),
                eq(dayAfter),
                eq(startTime),
                eq(endTime),
                anyLong()
        )).thenReturn(new ArrayList<>()); // No overlapping (different day)

        Appointment savedAppointment = Appointment.builder()
                .id(2L)
                .professional(professional)
                .client(client)
                .service(service)
                .date(dayAfter)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        // Act
        var result = appointmentService.createAppointment(request, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(dayAfter.toString(), result.date());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el cliente no existe")
    void testCreateAppointment_ThrowsException_WhenClientNotFound() {
        // Arrange
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L,
                LocalDate.now().plusDays(1).toString(),
                "10:00",
                null
        );

        when(clientRepository.findByUserId(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.createAppointment(request, 2L),
                "Debe lanzar excepción cuando el cliente no existe"
        );

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la fecha es pasada")
    void testCreateAppointment_ThrowsException_WhenPastDate() {
        // Arrange
        LocalDate yesterday = LocalDate.now().minusDays(1);
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L,
                yesterday.toString(),
                "10:00",
                null
        );

        when(clientRepository.findByUserId(2L)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(request, 2L),
                "Debe lanzar excepción cuando la fecha es pasada"
        );

        assertEquals("No se pueden agendar turnos en fechas pasadas", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }
}

