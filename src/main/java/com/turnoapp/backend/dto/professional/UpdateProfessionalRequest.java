package com.turnoapp.backend.dto.professional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateProfessionalRequest(
        String firstName,
        String lastName,

        @Email(message = "Email must be valid")
        String email,

        String profession,

        @Pattern(regexp = "^[a-z0-9-]+$", message = "Custom URL must contain only lowercase letters, numbers, and hyphens")
        String customUrl,

        String phone
) {
}
