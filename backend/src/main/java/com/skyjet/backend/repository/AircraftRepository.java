package com.skyjet.backend.repository;

import com.skyjet.backend.entity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    List<Aircraft> findByAirline_AirlineId(Long airlineId);
}
