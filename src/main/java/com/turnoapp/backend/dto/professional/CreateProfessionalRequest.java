package com.turnoapp.backend.dto.professional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProfessionalRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Profession is required")
        String profession,

        @NotBlank(message = "Custom URL is required")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Custom URL must contain only lowercase letters, numbers, and hyphens")
        String customUrl,

        String phone
) {
}
