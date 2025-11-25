package com.turnoapp.backend.dto.professional;

import com.turnoapp.backend.model.Professional;
import com.turnoapp.backend.model.enums.Status;

public record ProfessionalResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String profession,
        String customUrl,
        String phone,
        Status status,
        SiteConfigResponse siteConfig
) {
    public static ProfessionalResponse fromEntity(Professional professional) {
        return new ProfessionalResponse(
                professional.getId(),
                professional.getUser().getId(),
                professional.getUser().getFirstName(),
                professional.getUser().getLastName(),
                professional.getUser().getEmail(),
                professional.getProfession(),
                professional.getCustomUrl(),
                professional.getUser().getPhone(),
                professional.getUser().getStatus(),
                SiteConfigResponse.fromEntity(professional.getSiteConfig())
        );
    }
}
