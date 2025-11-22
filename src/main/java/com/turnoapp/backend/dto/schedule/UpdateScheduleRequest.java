package com.turnoapp.backend.dto.schedule;

import jakarta.validation.constraints.Pattern;

public record UpdateScheduleRequest(
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de inicio debe estar en formato HH:mm")
        String startTime,

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de fin debe estar en formato HH:mm")
        String endTime,

        Boolean active
) {
}
