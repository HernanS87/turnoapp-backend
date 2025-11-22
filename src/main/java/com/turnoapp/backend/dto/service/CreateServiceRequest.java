package com.turnoapp.backend.dto.service;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateServiceRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        String description,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        BigDecimal price,

        @NotNull(message = "La duración es obligatoria")
        @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
        Integer duration,

        @NotNull(message = "El porcentaje de seña es obligatorio")
        @Min(value = 0, message = "El porcentaje de seña debe ser al menos 0")
        @Max(value = 100, message = "El porcentaje de seña no puede exceder 100")
        Integer depositPercentage
) {
}
