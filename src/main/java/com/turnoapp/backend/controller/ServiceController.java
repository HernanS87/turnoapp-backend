package com.turnoapp.backend.controller;

import com.turnoapp.backend.config.security.CustomUserDetails;
import com.turnoapp.backend.dto.service.CreateServiceRequest;
import com.turnoapp.backend.dto.service.ServiceResponse;
import com.turnoapp.backend.dto.service.UpdateServiceRequest;
import com.turnoapp.backend.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROFESSIONAL')")
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getServices(Authentication authentication) {
        Long professionalId = getProfessionalId(authentication);
        List<ServiceResponse> services = serviceService.getServicesByProfessional(professionalId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professionalId = getProfessionalId(authentication);
        ServiceResponse service = serviceService.getServiceById(id, professionalId);
        return ResponseEntity.ok(service);
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody CreateServiceRequest request,
            Authentication authentication
    ) {
        Long professionalId = getProfessionalId(authentication);
        ServiceResponse service = serviceService.createService(request, professionalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request,
            Authentication authentication
    ) {
        Long professionalId = getProfessionalId(authentication);
        ServiceResponse service = serviceService.updateService(id, request, professionalId);
        return ResponseEntity.ok(service);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professionalId = getProfessionalId(authentication);
        serviceService.deleteService(id, professionalId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleServiceStatus(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professionalId = getProfessionalId(authentication);
        serviceService.toggleServiceStatus(id, professionalId);
        return ResponseEntity.noContent().build();
    }

    private Long getProfessionalId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long professionalId = userDetails.getProfessionalId();
        if (professionalId == null) {
            throw new RuntimeException("Professional ID not found in user details");
        }
        return professionalId;
    }
}
