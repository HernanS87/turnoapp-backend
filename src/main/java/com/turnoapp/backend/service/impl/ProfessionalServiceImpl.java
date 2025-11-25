package com.turnoapp.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turnoapp.backend.dto.professional.CreateProfessionalRequest;
import com.turnoapp.backend.dto.professional.ProfessionalResponse;
import com.turnoapp.backend.dto.professional.SiteConfigRequest;
import com.turnoapp.backend.dto.professional.UpdateProfessionalRequest;
import com.turnoapp.backend.exception.ResourceNotFoundException;
import com.turnoapp.backend.model.Professional;
import com.turnoapp.backend.model.SiteConfig;
import com.turnoapp.backend.model.enums.Status;
import com.turnoapp.backend.model.User;
import com.turnoapp.backend.model.enums.UserRole;
import com.turnoapp.backend.repository.ProfessionalRepository;
import com.turnoapp.backend.repository.SiteConfigRepository;
import com.turnoapp.backend.repository.UserRepository;
import com.turnoapp.backend.service.ProfessionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfessionalServiceImpl implements ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final SiteConfigRepository siteConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionalResponse> getAllProfessionals() {
        List<Professional> professionals = professionalRepository.findAllWithUser();
        // Cargar SiteConfig para cada profesional
        professionals.forEach(professional -> {
            if (professional.getSiteConfig() == null) {
                siteConfigRepository.findByProfessionalId(professional.getId())
                        .ifPresent(professional::setSiteConfig);
            }
        });
        return professionals.stream()
                .map(ProfessionalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalResponse getProfessionalById(Long id) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + id));

        // Cargar SiteConfig si existe
        if (professional.getSiteConfig() == null) {
            siteConfigRepository.findByProfessionalId(id)
                    .ifPresent(professional::setSiteConfig);
        }

        return ProfessionalResponse.fromEntity(professional);
    }

    @Override
    @Transactional
    public ProfessionalResponse createProfessional(CreateProfessionalRequest request) {
        // Validate unique email
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate unique custom URL
        if (professionalRepository.existsByCustomUrl(request.customUrl())) {
            throw new RuntimeException("Custom URL already exists");
        }

        // Create user
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.PROFESSIONAL)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .build();

        user = userRepository.save(user);

        // Create professional
        Professional professional = Professional.builder()
                .user(user)
                .profession(request.profession())
                .customUrl(request.customUrl())
                .build();

        professional = professionalRepository.save(professional);

        return ProfessionalResponse.fromEntity(professional);
    }

    @Override
    @Transactional
    public ProfessionalResponse updateProfessional(Long id, UpdateProfessionalRequest request) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + id));

        User user = professional.getUser();

        // Update user fields if provided
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.email());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        // Update professional fields if provided
        if (request.profession() != null) {
            professional.setProfession(request.profession());
        }
        if (request.customUrl() != null && !request.customUrl().equals(professional.getCustomUrl())) {
            if (professionalRepository.existsByCustomUrl(request.customUrl())) {
                throw new RuntimeException("Custom URL already exists");
            }
            professional.setCustomUrl(request.customUrl());
        }

        userRepository.save(user);
        professionalRepository.save(professional);

        return ProfessionalResponse.fromEntity(professional);
    }

    @Override
    @Transactional
    public void toggleProfessionalStatus(Long id) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + id));

        User user = professional.getUser();
        user.setStatus(user.getStatus() == Status.ACTIVE ? Status.INACTIVE : Status.ACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public ProfessionalResponse updateSiteConfig(Long professionalId, SiteConfigRequest request) {
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + professionalId));

        // Obtener o crear SiteConfig
        SiteConfig siteConfig = siteConfigRepository.findByProfessionalId(professionalId)
                .orElse(SiteConfig.builder()
                        .professional(professional)
                        .build());

        // Actualizar campos
        if (request.logoUrl() != null) {
            siteConfig.setLogoUrl(request.logoUrl());
        }
        if (request.primaryColor() != null) {
            siteConfig.setPrimaryColor(request.primaryColor());
        }
        if (request.secondaryColor() != null) {
            siteConfig.setSecondaryColor(request.secondaryColor());
        }
        if (request.professionalDescription() != null) {
            siteConfig.setProfessionalDescription(request.professionalDescription());
        }
        if (request.address() != null) {
            siteConfig.setAddress(request.address());
        }
        if (request.city() != null) {
            siteConfig.setCity(request.city());
        }
        if (request.province() != null) {
            siteConfig.setProvince(request.province());
        }
        if (request.country() != null) {
            siteConfig.setCountry(request.country());
        }
        if (request.businessHours() != null) {
            siteConfig.setBusinessHours(request.businessHours());
        }
        if (request.welcomeMessage() != null) {
            siteConfig.setWelcomeMessage(request.welcomeMessage());
        }
        if (request.socialMedia() != null) {
            try {
                String json = objectMapper.writeValueAsString(request.socialMedia());
                siteConfig.setSocialMedia(json);
            } catch (Exception e) {
                throw new RuntimeException("Error serializing social media", e);
            }
        }

        siteConfigRepository.save(siteConfig);

        // Actualizar la relación en Professional
        professional.setSiteConfig(siteConfig);
        professionalRepository.save(professional);

        return ProfessionalResponse.fromEntity(professional);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalResponse getProfessionalByCustomUrl(String customUrl) {
        Professional professional = professionalRepository.findByCustomUrl(customUrl)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with custom URL: " + customUrl));

        // Cargar SiteConfig si existe
        if (professional.getSiteConfig() == null) {
            siteConfigRepository.findByProfessionalId(professional.getId())
                    .ifPresent(professional::setSiteConfig);
        }

        return ProfessionalResponse.fromEntity(professional);
    }
}
