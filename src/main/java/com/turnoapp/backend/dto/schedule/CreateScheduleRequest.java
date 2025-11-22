package com.turnoapp.backend.dto.schedule;

import jakarta.validation.constraints.*;

public record CreateScheduleRequest(
        @NotNull(message = "El día de la semana es obligatorio")
        @Min(value = 0, message = "El día debe ser entre 0 (Domingo) y 6 (Sábado)")
        @Max(value = 6, message = "El día debe ser entre 0 (Domingo) y 6 (Sábado)")
        Integer dayOfWeek,

        @NotBlank(message = "La hora de inicio es obligatoria")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de inicio debe estar en formato HH:mm")
        String startTime,

        @NotBlank(message = "La hora de fin es obligatoria")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de fin debe estar en formato HH:mm")
        String endTime
) {
}
