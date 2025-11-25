package com.turnoapp.backend.repository;

import com.turnoapp.backend.model.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteConfigRepository extends JpaRepository<SiteConfig, Long> {

    Optional<SiteConfig> findByProfessionalId(Long professionalId);

    @Query("SELECT sc FROM SiteConfig sc JOIN FETCH sc.professional WHERE sc.professional.id = :professionalId")
    Optional<SiteConfig> findByProfessionalIdWithProfessional(Long professionalId);

    boolean existsByProfessionalId(Long professionalId);
}

