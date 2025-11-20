package com.turnoapp.backend.controller;

import com.turnoapp.backend.dto.professional.CreateProfessionalRequest;
import com.turnoapp.backend.dto.professional.ProfessionalResponse;
import com.turnoapp.backend.dto.professional.UpdateProfessionalRequest;
import com.turnoapp.backend.service.ProfessionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/professionals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ProfessionalService professionalService;

    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> getAllProfessionals() {
        List<ProfessionalResponse> professionals = professionalService.getAllProfessionals();
        return ResponseEntity.ok(professionals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> getProfessionalById(@PathVariable Long id) {
        ProfessionalResponse professional = professionalService.getProfessionalById(id);
        return ResponseEntity.ok(professional);
    }

    @PostMapping
    public ResponseEntity<ProfessionalResponse> createProfessional(
            @Valid @RequestBody CreateProfessionalRequest request
    ) {
        ProfessionalResponse professional = professionalService.createProfessional(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(professional);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> updateProfessional(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfessionalRequest request
    ) {
        ProfessionalResponse professional = professionalService.updateProfessional(id, request);
        return ResponseEntity.ok(professional);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleProfessionalStatus(@PathVariable Long id) {
        professionalService.toggleProfessionalStatus(id);
        return ResponseEntity.noContent().build();
    }
}
