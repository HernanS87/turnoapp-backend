package com.turnoapp.backend.repository;

import com.turnoapp.backend.model.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    Optional<Professional> findByCustomUrl(String customUrl);

    boolean existsByCustomUrl(String customUrl);

    Optional<Professional> findByUserId(Long userId);

    @Query("SELECT p FROM Professional p JOIN FETCH p.user")
    List<Professional> findAllWithUser();
}
