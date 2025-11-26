package com.turnoapp.backend.repository;

import com.turnoapp.backend.model.Professional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT DISTINCT p FROM Professional p " +
           "JOIN p.user u " +
           "LEFT JOIN p.siteConfig sc " +
           "WHERE u.status = 'ACTIVE' " +
           "AND (:profession IS NULL OR LOWER(p.profession) LIKE LOWER(CONCAT('%', :profession, '%'))) " +
           "AND (:province IS NULL OR LOWER(TRIM(u.province)) = LOWER(TRIM(:province))) " +
           "AND (:city IS NULL OR LOWER(TRIM(u.city)) = LOWER(TRIM(:city))) " +
           "AND (:search IS NULL OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Professional> findActiveProfessionalsWithFilters(
            @Param("profession") String profession,
            @Param("province") String province,
            @Param("city") String city,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p.profession FROM Professional p " +
           "JOIN p.user u " +
           "WHERE u.status = 'ACTIVE' AND p.profession IS NOT NULL AND p.profession != '' " +
           "ORDER BY p.profession")
    List<String> findDistinctProfessions();

    @Query("SELECT DISTINCT u.province FROM Professional p " +
           "JOIN p.user u " +
           "WHERE u.status = 'ACTIVE' AND u.province IS NOT NULL AND u.province != '' " +
           "ORDER BY u.province")
    List<String> findDistinctProvinces();

    @Query("SELECT DISTINCT u.city FROM Professional p " +
           "JOIN p.user u " +
           "WHERE u.status = 'ACTIVE' AND u.city IS NOT NULL AND u.city != '' " +
           "ORDER BY u.city")
    List<String> findDistinctCities();
}
