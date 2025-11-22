package com.turnoapp.backend.repository;

import com.turnoapp.backend.model.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleSlot, Long> {

    List<ScheduleSlot> findByProfessionalId(Long professionalId);

    List<ScheduleSlot> findByProfessionalIdAndActiveTrue(Long professionalId);

    Optional<ScheduleSlot> findByIdAndProfessionalId(Long id, Long professionalId);

    List<ScheduleSlot> findByProfessionalIdAndDayOfWeekAndActiveTrue(Long professionalId, Integer dayOfWeek);

    @Query("SELECT s FROM ScheduleSlot s WHERE s.professional.id = :professionalId " +
           "AND s.dayOfWeek = :dayOfWeek " +
           "AND s.active = true " +
           "AND s.id != :excludeId " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<ScheduleSlot> findOverlappingSlots(
            @Param("professionalId") Long professionalId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("excludeId") Long excludeId
    );
}
