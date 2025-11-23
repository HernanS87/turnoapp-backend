package com.turnoapp.backend.service;

import com.turnoapp.backend.dto.service.CreateServiceRequest;
import com.turnoapp.backend.dto.service.ServiceResponse;
import com.turnoapp.backend.dto.service.UpdateServiceRequest;

import java.util.List;

public interface ServiceService {
    List<ServiceResponse> getServicesByProfessional(Long professionalId);
    List<ServiceResponse> getServicesByCustomUrl(String customUrl);
    ServiceResponse getServiceByCustomUrlAndId(String customUrl, Long serviceId);
    ServiceResponse getServiceById(Long id, Long professionalId);
    ServiceResponse createService(CreateServiceRequest request, Long professionalId);
    ServiceResponse updateService(Long id, UpdateServiceRequest request, Long professionalId);
    void deleteService(Long id, Long professionalId);
    void toggleServiceStatus(Long id, Long professionalId);
}
