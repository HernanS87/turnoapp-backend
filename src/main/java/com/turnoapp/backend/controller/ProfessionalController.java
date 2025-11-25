package com.turnoapp.backend.controller;

import com.turnoapp.backend.config.security.CustomUserDetails;
import com.turnoapp.backend.dto.professional.ProfessionalResponse;
import com.turnoapp.backend.dto.professional.SiteConfigRequest;
import com.turnoapp.backend.service.ProfessionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professionals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROFESSIONAL')")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    @GetMapping("/me")
    public ResponseEntity<ProfessionalResponse> getMyProfile(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long professionalId = userDetails.getProfessionalId();
        
        if (professionalId == null) {
            throw new RuntimeException("Professional ID not found in user details");
        }
        
        ProfessionalResponse professional = professionalService.getProfessionalById(professionalId);
        return ResponseEntity.ok(professional);
    }

    @PutMapping("/me/site-config")
    public ResponseEntity<ProfessionalResponse> updateSiteConfig(
            @Valid @RequestBody SiteConfigRequest request,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long professionalId = userDetails.getProfessionalId();
        
        if (professionalId == null) {
            throw new RuntimeException("Professional ID not found in user details");
        }
        
        ProfessionalResponse professional = professionalService.updateSiteConfig(professionalId, request);
        return ResponseEntity.ok(professional);
    }

    @GetMapping("/public/by-url/{customUrl}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ProfessionalResponse> getProfessionalByCustomUrl(
            @PathVariable String customUrl
    ) {
        ProfessionalResponse professional = professionalService.getProfessionalByCustomUrl(customUrl);
        return ResponseEntity.ok(professional);
    }
}

