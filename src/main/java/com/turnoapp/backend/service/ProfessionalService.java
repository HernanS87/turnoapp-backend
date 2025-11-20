package com.turnoapp.backend.service;

import com.turnoapp.backend.dto.professional.CreateProfessionalRequest;
import com.turnoapp.backend.dto.professional.ProfessionalResponse;
import com.turnoapp.backend.dto.professional.UpdateProfessionalRequest;

import java.util.List;

public interface ProfessionalService {
    List<ProfessionalResponse> getAllProfessionals();
    ProfessionalResponse getProfessionalById(Long id);
    ProfessionalResponse createProfessional(CreateProfessionalRequest request);
    ProfessionalResponse updateProfessional(Long id, UpdateProfessionalRequest request);
    void toggleProfessionalStatus(Long id);
}
