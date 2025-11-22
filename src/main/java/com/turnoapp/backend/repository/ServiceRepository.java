package com.turnoapp.backend.repository;

import com.turnoapp.backend.model.Service;
import com.turnoapp.backend.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByProfessionalId(Long professionalId);
    List<Service> findByProfessionalIdAndStatus(Long professionalId, Status status);
    Optional<Service> findByIdAndProfessionalId(Long id, Long professionalId);
}
