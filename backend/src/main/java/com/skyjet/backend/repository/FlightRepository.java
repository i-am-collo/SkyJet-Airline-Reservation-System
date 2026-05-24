package com.skyjet.backend.repository;

import com.skyjet.backend.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FlightRepository - Data access layer for Flight entities
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findByFlightNumber(String flightNumber);

    @Query("SELECT f FROM Flight f WHERE LOWER(f.origin) LIKE LOWER(CONCAT('%', :origin, '%')) " +
            "AND LOWER(f.destination) LIKE LOWER(CONCAT('%', :destination, '%'))")
    List<Flight> searchByOriginAndDestination(@Param("origin") String origin,
            @Param("destination") String destination);

    @Query("SELECT f FROM Flight f WHERE f.status NOT IN ('CANCELLED')")
    List<Flight> findAllActive();

    List<Flight> findByStatus(String status);
}
