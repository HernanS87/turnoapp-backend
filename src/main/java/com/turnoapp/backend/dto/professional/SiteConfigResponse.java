package com.turnoapp.backend.dto.professional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turnoapp.backend.model.SiteConfig;

public record SiteConfigResponse(
        String logoUrl,
        String primaryColor,
        String secondaryColor,
        String professionalDescription,
        String address,
        String city,
        String province,
        String country,
        String businessHours,
        String welcomeMessage,
        SocialMediaResponse socialMedia
) {
    public static SiteConfigResponse fromEntity(SiteConfig siteConfig) {
        if (siteConfig == null) {
            return createDefault();
        }
        
        return new SiteConfigResponse(
                siteConfig.getLogoUrl(),
                siteConfig.getPrimaryColor(),
                siteConfig.getSecondaryColor(),
                siteConfig.getProfessionalDescription(),
                siteConfig.getAddress(),
                siteConfig.getCity(),
                siteConfig.getProvince(),
                siteConfig.getCountry(),
                siteConfig.getBusinessHours(),
                siteConfig.getWelcomeMessage(),
                SocialMediaResponse.fromJson(siteConfig.getSocialMedia())
        );
    }

    public static SiteConfigResponse createDefault() {
        return new SiteConfigResponse(
                "/assets/logo-default.png",
                "#6366f1",
                "#8b5cf6",
                null,
                null,
                null,
                null,
                "Argentina",
                null,
                "Bienvenido",
                new SocialMediaResponse(null, null, null)
        );
    }

    public record SocialMediaResponse(
            String instagram,
            String facebook,
            String linkedin
    ) {
        public static SocialMediaResponse fromJson(String json) {
            if (json == null || json.isEmpty()) {
                return new SocialMediaResponse(null, null, null);
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, SocialMediaResponse.class);
            } catch (Exception e) {
                return new SocialMediaResponse(null, null, null);
            }
        }
    }
}

