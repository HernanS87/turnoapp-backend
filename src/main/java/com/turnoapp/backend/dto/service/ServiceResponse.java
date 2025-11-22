package com.turnoapp.backend.dto.service;

import com.turnoapp.backend.model.Service;
import com.turnoapp.backend.model.enums.Status;

import java.math.BigDecimal;

public record ServiceResponse(
        Long id,
        Long professionalId,
        String name,
        String description,
        BigDecimal price,
        Integer duration,
        Integer depositPercentage,
        Status status
) {
    public static ServiceResponse fromEntity(Service service) {
        return new ServiceResponse(
                service.getId(),
                service.getProfessional().getId(),
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getDuration(),
                service.getDepositPercentage(),
                service.getStatus()
        );
    }
}
