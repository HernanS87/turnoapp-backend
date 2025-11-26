package com.turnoapp.backend.controller;

import com.turnoapp.backend.config.security.CustomUserDetails;
import com.turnoapp.backend.dto.professional.FilterOptionsResponse;
import com.turnoapp.backend.dto.professional.ProfessionalResponse;
import com.turnoapp.backend.dto.professional.SiteConfigRequest;
import com.turnoapp.backend.service.ProfessionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping("/public/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<ProfessionalResponse>> searchProfessionals(
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Page<ProfessionalResponse> result = professionalService.searchProfessionals(
                profession, province, city, search, page, size
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/public/filter-options")
    @PreAuthorize("permitAll()")
    public ResponseEntity<FilterOptionsResponse> getFilterOptions() {
        FilterOptionsResponse options = professionalService.getFilterOptions();
        return ResponseEntity.ok(options);
    }
}

