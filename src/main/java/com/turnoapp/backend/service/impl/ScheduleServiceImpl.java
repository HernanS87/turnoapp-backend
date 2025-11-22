package com.turnoapp.backend.service.impl;

import com.turnoapp.backend.dto.schedule.CreateScheduleRequest;
import com.turnoapp.backend.dto.schedule.ScheduleResponse;
import com.turnoapp.backend.dto.schedule.UpdateScheduleRequest;
import com.turnoapp.backend.exception.ResourceNotFoundException;
import com.turnoapp.backend.model.Professional;
import com.turnoapp.backend.model.ScheduleSlot;
import com.turnoapp.backend.repository.ProfessionalRepository;
import com.turnoapp.backend.repository.ScheduleRepository;
import com.turnoapp.backend.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ProfessionalRepository professionalRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getScheduleByProfessional(Long professionalId) {
        return scheduleRepository.findByProfessionalId(professionalId).stream()
                .map(ScheduleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(Long id, Long professionalId) {
        ScheduleSlot slot = scheduleRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule slot not found with id: " + id));

        return ScheduleResponse.fromEntity(slot);
    }

    @Override
    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request, Long professionalId) {
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + professionalId));

        // Validate time range
        validateTimeRange(request.startTime(), request.endTime());

        // Validate no overlapping slots
        validateNoOverlap(professionalId, request.dayOfWeek(), request.startTime(), request.endTime(), null);

        ScheduleSlot slot = ScheduleSlot.builder()
                .professional(professional)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .active(true)
                .build();

        slot = scheduleRepository.save(slot);

        return ScheduleResponse.fromEntity(slot);
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Long id, UpdateScheduleRequest request, Long professionalId) {
        ScheduleSlot slot = scheduleRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule slot not found with id: " + id));

        String newStartTime = request.startTime() != null ? request.startTime() : slot.getStartTime();
        String newEndTime = request.endTime() != null ? request.endTime() : slot.getEndTime();

        // Validate time range if times are being updated
        if (request.startTime() != null || request.endTime() != null) {
            validateTimeRange(newStartTime, newEndTime);

            // Validate no overlapping slots (excluding current slot)
            validateNoOverlap(professionalId, slot.getDayOfWeek(), newStartTime, newEndTime, id);
        }

        if (request.startTime() != null) {
            slot.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            slot.setEndTime(request.endTime());
        }
        if (request.active() != null) {
            slot.setActive(request.active());
        }

        slot = scheduleRepository.save(slot);

        return ScheduleResponse.fromEntity(slot);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id, Long professionalId) {
        ScheduleSlot slot = scheduleRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule slot not found with id: " + id));

        scheduleRepository.delete(slot);
    }

    /**
     * Validates that start time is before end time
     */
    private void validateTimeRange(String startTime, String endTime) {
        if (startTime.compareTo(endTime) >= 0) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    /**
     * Validates that the new schedule slot does not overlap with existing ones
     */
    private void validateNoOverlap(Long professionalId, Integer dayOfWeek, String startTime, String endTime, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;

        List<ScheduleSlot> overlappingSlots = scheduleRepository.findOverlappingSlots(
                professionalId,
                dayOfWeek,
                startTime,
                endTime,
                excludeIdSafe
        );

        if (!overlappingSlots.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("El horario se solapa con un bloque existente. " +
                            "Ya existe un bloque entre %s y %s para este día",
                            overlappingSlots.get(0).getStartTime(),
                            overlappingSlots.get(0).getEndTime())
            );
        }
    }
}
