package com.turnoapp.backend.dto.schedule;

import com.turnoapp.backend.model.ScheduleSlot;

import java.time.Instant;

public record ScheduleResponse(
        Long id,
        Long professionalId,
        Integer dayOfWeek,
        String startTime,
        String endTime,
        Boolean active,
        Instant createdAt
) {
    public static ScheduleResponse fromEntity(ScheduleSlot slot) {
        return new ScheduleResponse(
                slot.getId(),
                slot.getProfessional().getId(),
                slot.getDayOfWeek(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getActive(),
                slot.getCreatedAt()
        );
    }
}
