package com.skyjet.backend.repository;

import com.skyjet.backend.entity.Flight;
import com.skyjet.backend.entity.enums.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FlightRepository - Data access layer for Flight entities
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f " +
           "JOIN FETCH f.airline " +
           "JOIN FETCH f.aircraft " +
           "JOIN FETCH f.departureAirport " +
           "JOIN FETCH f.arrivalAirport " +
           "WHERE f.status <> com.skyjet.backend.entity.enums.FlightStatus.CANCELLED")
    List<Flight> findAllActive();

    @Query("SELECT f FROM Flight f " +
           "JOIN FETCH f.airline " +
           "JOIN FETCH f.aircraft " +
           "JOIN FETCH f.departureAirport da " +
           "JOIN FETCH f.arrivalAirport aa " +
           "WHERE f.status <> com.skyjet.backend.entity.enums.FlightStatus.CANCELLED " +
           "AND (LOWER(da.city) LIKE LOWER(CONCAT('%', :origin, '%')) " +
           "     OR LOWER(da.iataCode) LIKE LOWER(CONCAT('%', :origin, '%'))) " +
           "AND (LOWER(aa.city) LIKE LOWER(CONCAT('%', :destination, '%')) " +
           "     OR LOWER(aa.iataCode) LIKE LOWER(CONCAT('%', :destination, '%')))")
    List<Flight> searchByOriginAndDestination(@Param("origin") String origin,
                                              @Param("destination") String destination);

    @Query("SELECT f FROM Flight f " +
           "JOIN FETCH f.airline " +
           "JOIN FETCH f.aircraft " +
           "JOIN FETCH f.departureAirport " +
           "JOIN FETCH f.arrivalAirport " +
           "WHERE f.flightId = :id")
    Flight findByIdWithDetails(@Param("id") Long id);

    List<Flight> findByStatus(FlightStatus status);
}
