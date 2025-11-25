package com.turnoapp.backend.dto.professional;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SiteConfigRequest(
        String logoUrl,
        
        @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "Primary color must be a valid hex color")
        String primaryColor,
        
        @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "Secondary color must be a valid hex color")
        String secondaryColor,
        
        @Size(max = 2000, message = "Professional description must not exceed 2000 characters")
        String professionalDescription,
        
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,
        
        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,
        
        @Size(max = 100, message = "Province must not exceed 100 characters")
        String province,
        
        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,
        
        @Size(max = 255, message = "Business hours must not exceed 255 characters")
        String businessHours,
        
        @Size(max = 500, message = "Welcome message must not exceed 500 characters")
        String welcomeMessage,
        
        SocialMediaRequest socialMedia
) {
    public record SocialMediaRequest(
            String instagram,
            String facebook,
            String linkedin
    ) {}
}

