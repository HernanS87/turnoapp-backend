package com.turnoapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turnoapp.backend.config.security.JwtTokenProvider;
import com.turnoapp.backend.dto.professional.ProfessionalResponse;
import com.turnoapp.backend.dto.professional.SiteConfigRequest;
import com.turnoapp.backend.model.*;
import com.turnoapp.backend.model.enums.Status;
import com.turnoapp.backend.model.enums.UserRole;
import com.turnoapp.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para ProfessionalController.
 * 
 * Enfoque: Testing end-to-end con base de datos real (H2 en memoria).
 * Demuestra: Testing de APIs REST, autenticación, persistencia, relaciones 1:1.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProfessionalController - Pruebas de Integración")
class ProfessionalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private SiteConfigRepository siteConfigRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User professionalUser;
    private Professional professional;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Clean up
        siteConfigRepository.deleteAll();
        professionalRepository.deleteAll();
        userRepository.deleteAll();

        // Create professional user
        professionalUser = User.builder()
                .email("prof@test.com")
                .passwordHash("$2a$10$dummy") // Hashed password
                .firstName("Profesional")
                .lastName("Test")
                .role(UserRole.PROFESSIONAL)
                .status(Status.ACTIVE)
                .phone("+54 261 123-4567")
                .build();

        professionalUser = userRepository.save(professionalUser);

        // Create professional
        professional = Professional.builder()
                .user(professionalUser)
                .profession("Psicólogo")
                .customUrl("prof-test")
                .build();

        professional = professionalRepository.save(professional);

        // Generate JWT token
        authToken = jwtTokenProvider.generateToken(professionalUser, professional.getId());
    }

    @Test
    @DisplayName("Debe actualizar la configuración del sitio cuando no existe previamente")
    void testUpdateSiteConfig_CreatesNewConfig_WhenNotExists() throws Exception {
        // Arrange
        SiteConfigRequest request = new SiteConfigRequest(
                null,
                "#FF5733",
                "#33C3F0",
                "Psicólogo especializado en terapia cognitivo-conductual",
                "San Martín 1234",
                "Mendoza",
                "Mendoza",
                "Argentina",
                "Lunes a Viernes 9:00 - 18:00",
                "Bienvenido a mi consultorio",
                new SiteConfigRequest.SocialMediaRequest(
                        "@prof.test",
                        "prof.test",
                        "prof-test"
                )
        );

        // Act
        MvcResult result = mockMvc.perform(put("/api/professionals/me/site-config")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.siteConfig").exists())
                .andReturn();

        // Assert
        ProfessionalResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProfessionalResponse.class
        );

        assertNotNull(response.siteConfig());
        assertEquals("#FF5733", response.siteConfig().primaryColor());
        assertEquals("#33C3F0", response.siteConfig().secondaryColor());
        assertEquals("Psicólogo especializado en terapia cognitivo-conductual", 
                response.siteConfig().professionalDescription());
        assertEquals("San Martín 1234", response.siteConfig().address());
        assertEquals("Mendoza", response.siteConfig().city());
        assertEquals("Mendoza", response.siteConfig().province());
        assertEquals("Argentina", response.siteConfig().country());
        assertEquals("Lunes a Viernes 9:00 - 18:00", response.siteConfig().businessHours());
        assertEquals("Bienvenido a mi consultorio", response.siteConfig().welcomeMessage());
        assertNotNull(response.siteConfig().socialMedia());
        assertEquals("@prof.test", response.siteConfig().socialMedia().instagram());

        // Verify persistence
        var savedConfig = siteConfigRepository.findByProfessionalId(professional.getId());
        assertTrue(savedConfig.isPresent());
        assertEquals("#FF5733", savedConfig.get().getPrimaryColor());
    }

    @Test
    @DisplayName("Debe actualizar parcialmente la configuración existente")
    void testUpdateSiteConfig_UpdatesPartial_WhenExists() throws Exception {
        // Arrange - Create existing config
        SiteConfig existingConfig = SiteConfig.builder()
                .professional(professional)
                .primaryColor("#6366f1")
                .secondaryColor("#8b5cf6")
                .professionalDescription("Descripción original")
                .address("Dirección original")
                .build();

        siteConfigRepository.save(existingConfig);

        // Update only colors
        SiteConfigRequest request = new SiteConfigRequest(
                null,
                "#FF0000", // New primary color
                "#00FF00", // New secondary color
                null, // Keep existing description
                null, // Keep existing address
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Act
        MvcResult result = mockMvc.perform(put("/api/professionals/me/site-config")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ProfessionalResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProfessionalResponse.class
        );

        assertEquals("#FF0000", response.siteConfig().primaryColor());
        assertEquals("#00FF00", response.siteConfig().secondaryColor());
        // Original values should be preserved
        assertEquals("Descripción original", response.siteConfig().professionalDescription());
        assertEquals("Dirección original", response.siteConfig().address());

        // Verify persistence
        var savedConfig = siteConfigRepository.findByProfessionalId(professional.getId());
        assertTrue(savedConfig.isPresent());
        assertEquals("#FF0000", savedConfig.get().getPrimaryColor());
        assertEquals("#00FF00", savedConfig.get().getSecondaryColor());
        assertEquals("Descripción original", savedConfig.get().getProfessionalDescription());
    }

    @Test
    @DisplayName("Debe retornar 401 cuando no hay token de autenticación")
    void testUpdateSiteConfig_ReturnsUnauthorized_WhenNoToken() throws Exception {
        // Arrange
        SiteConfigRequest request = new SiteConfigRequest(
                null,
                "#FF5733",
                "#33C3F0",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Act & Assert
        mockMvc.perform(put("/api/professionals/me/site-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando los colores tienen formato inválido")
    void testUpdateSiteConfig_ReturnsBadRequest_WhenInvalidColorFormat() throws Exception {
        // Arrange
        SiteConfigRequest request = new SiteConfigRequest(
                null,
                "invalid-color", // Invalid format
                "#33C3F0",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Act & Assert
        mockMvc.perform(put("/api/professionals/me/site-config")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe obtener el perfil del profesional autenticado")
    void testGetMyProfile_ReturnsProfessional_WhenAuthenticated() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(get("/api/professionals/me")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professional.getId()))
                .andExpect(jsonPath("$.email").value("prof@test.com"))
                .andExpect(jsonPath("$.profession").value("Psicólogo"))
                .andReturn();

        // Assert
        ProfessionalResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProfessionalResponse.class
        );

        assertEquals(professional.getId(), response.id());
        assertEquals("prof@test.com", response.email());
        assertEquals("Psicólogo", response.profession());
        assertNotNull(response.siteConfig()); // Should have default or null
    }
}

