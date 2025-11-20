package com.turnoapp.backend.repository;

import com.turnoapp.backend.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUserId(Long userId);

    @Query("SELECT c FROM Client c JOIN FETCH c.user")
    List<Client> findAllWithUser();
}
