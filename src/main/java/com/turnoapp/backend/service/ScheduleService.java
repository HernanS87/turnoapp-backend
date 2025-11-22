package com.turnoapp.backend.service;

import com.turnoapp.backend.dto.schedule.CreateScheduleRequest;
import com.turnoapp.backend.dto.schedule.ScheduleResponse;
import com.turnoapp.backend.dto.schedule.UpdateScheduleRequest;

import java.util.List;

public interface ScheduleService {
    List<ScheduleResponse> getScheduleByProfessional(Long professionalId);
    ScheduleResponse getScheduleById(Long id, Long professionalId);
    ScheduleResponse createSchedule(CreateScheduleRequest request, Long professionalId);
    ScheduleResponse updateSchedule(Long id, UpdateScheduleRequest request, Long professionalId);
    void deleteSchedule(Long id, Long professionalId);
}
