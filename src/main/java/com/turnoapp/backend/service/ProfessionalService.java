package com.turnoapp.backend.service;

import com.turnoapp.backend.dto.professional.CreateProfessionalRequest;
import com.turnoapp.backend.dto.professional.FilterOptionsResponse;
import com.turnoapp.backend.dto.professional.ProfessionalResponse;
import com.turnoapp.backend.dto.professional.SiteConfigRequest;
import com.turnoapp.backend.dto.professional.UpdateProfessionalRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProfessionalService {
    List<ProfessionalResponse> getAllProfessionals();
    ProfessionalResponse getProfessionalById(Long id);
    ProfessionalResponse createProfessional(CreateProfessionalRequest request);
    ProfessionalResponse updateProfessional(Long id, UpdateProfessionalRequest request);
    void toggleProfessionalStatus(Long id);
    ProfessionalResponse updateSiteConfig(Long professionalId, SiteConfigRequest request);
    ProfessionalResponse getProfessionalByCustomUrl(String customUrl);
    Page<ProfessionalResponse> searchProfessionals(String profession, String province, String city, String search, int page, int size);
    FilterOptionsResponse getFilterOptions();
}
