package com.turnoapp.backend.controller;

import com.turnoapp.backend.config.security.CustomUserDetails;
import com.turnoapp.backend.dto.schedule.CreateScheduleRequest;
import com.turnoapp.backend.dto.schedule.ScheduleResponse;
import com.turnoapp.backend.dto.schedule.UpdateScheduleRequest;
import com.turnoapp.backend.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROFESSIONAL')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedule(Authentication authentication) {
        Long professionalId = getProfessionalId(authentication);
        List<ScheduleResponse> schedule = scheduleService.getScheduleByProfessional(professionalId);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professionalId = getProfessionalId(authentication);
        ScheduleResponse slot = scheduleService.getScheduleById(id, professionalId);
        return ResponseEntity.ok(slot);
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request,
            Authentication authentication
    ) {
        Long professionalId = getProfessionalId(authentication);
        ScheduleResponse slot = scheduleService.createSchedule(request, professionalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(slot);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleRequest request,
            Authentication authentication
    ) {
        Long professionalId = getProfessionalId(authentication);
        ScheduleResponse slot = scheduleService.updateSchedule(id, request, professionalId);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professionalId = getProfessionalId(authentication);
        scheduleService.deleteSchedule(id, professionalId);
        return ResponseEntity.noContent().build();
    }

    private Long getProfessionalId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long professionalId = userDetails.getProfessionalId();
        if (professionalId == null) {
            throw new RuntimeException("Professional ID not found in user details");
        }
        return professionalId;
    }
}
