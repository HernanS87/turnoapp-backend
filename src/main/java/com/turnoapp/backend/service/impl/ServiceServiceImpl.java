package com.turnoapp.backend.service.impl;

import com.turnoapp.backend.dto.service.CreateServiceRequest;
import com.turnoapp.backend.dto.service.ServiceResponse;
import com.turnoapp.backend.dto.service.UpdateServiceRequest;
import com.turnoapp.backend.exception.ResourceNotFoundException;
import com.turnoapp.backend.model.Professional;
import com.turnoapp.backend.model.Service;
import com.turnoapp.backend.model.enums.Status;
import com.turnoapp.backend.repository.ProfessionalRepository;
import com.turnoapp.backend.repository.ServiceRepository;
import com.turnoapp.backend.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ProfessionalRepository professionalRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesByProfessional(Long professionalId) {
        return serviceRepository.findByProfessionalId(professionalId).stream()
                .map(ServiceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id, Long professionalId) {
        Service service = serviceRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        return ServiceResponse.fromEntity(service);
    }

    @Override
    @Transactional
    public ServiceResponse createService(CreateServiceRequest request, Long professionalId) {
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + professionalId));

        Service service = Service.builder()
                .professional(professional)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .duration(request.duration())
                .depositPercentage(request.depositPercentage())
                .status(Status.ACTIVE)
                .build();

        service = serviceRepository.save(service);

        return ServiceResponse.fromEntity(service);
    }

    @Override
    @Transactional
    public ServiceResponse updateService(Long id, UpdateServiceRequest request, Long professionalId) {
        Service service = serviceRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        if (request.name() != null) {
            service.setName(request.name());
        }
        if (request.description() != null) {
            service.setDescription(request.description());
        }
        if (request.price() != null) {
            service.setPrice(request.price());
        }
        if (request.duration() != null) {
            service.setDuration(request.duration());
        }
        if (request.depositPercentage() != null) {
            service.setDepositPercentage(request.depositPercentage());
        }

        service = serviceRepository.save(service);

        return ServiceResponse.fromEntity(service);
    }

    @Override
    @Transactional
    public void deleteService(Long id, Long professionalId) {
        Service service = serviceRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        // Soft delete
        service.setStatus(Status.INACTIVE);
        serviceRepository.save(service);
    }

    @Override
    @Transactional
    public void toggleServiceStatus(Long id, Long professionalId) {
        Service service = serviceRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        // Toggle status: ACTIVE ↔ INACTIVE
        service.setStatus(service.getStatus() == Status.ACTIVE ? Status.INACTIVE : Status.ACTIVE);
        serviceRepository.save(service);
    }
}
