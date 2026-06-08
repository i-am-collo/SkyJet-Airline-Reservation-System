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
           "WHERE f.status <> :excludedStatus")
    List<Flight> findAllActiveExcluding(@Param("excludedStatus") FlightStatus excludedStatus);

    default List<Flight> findAllActive() {
        return findAllActiveExcluding(FlightStatus.CANCELLED);
    }

    @Query("SELECT f FROM Flight f " +
           "JOIN FETCH f.airline " +
           "JOIN FETCH f.aircraft " +
           "JOIN FETCH f.departureAirport da " +
           "JOIN FETCH f.arrivalAirport aa " +
           "WHERE f.status <> :excludedStatus " +
           "AND (:origin = '' OR LOWER(da.city) LIKE LOWER(CONCAT('%', :origin, '%')) " +
           "     OR LOWER(da.iataCode) LIKE LOWER(CONCAT('%', :origin, '%')) " +
           "     OR LOWER(CONCAT(da.city, ' (', TRIM(da.iataCode), ')')) LIKE LOWER(CONCAT('%', :origin, '%'))) " +
           "AND (:destination = '' OR LOWER(aa.city) LIKE LOWER(CONCAT('%', :destination, '%')) " +
           "     OR LOWER(aa.iataCode) LIKE LOWER(CONCAT('%', :destination, '%')) " +
           "     OR LOWER(CONCAT(aa.city, ' (', TRIM(aa.iataCode), ')')) LIKE LOWER(CONCAT('%', :destination, '%')))")
    List<Flight> searchByOriginAndDestinationExcluding(@Param("origin") String origin,
                                                       @Param("destination") String destination,
                                                       @Param("excludedStatus") FlightStatus excludedStatus);

    default List<Flight> searchByOriginAndDestination(String origin, String destination) {
        return searchByOriginAndDestinationExcluding(origin, destination, FlightStatus.CANCELLED);
    }

    @Query("SELECT f FROM Flight f " +
           "JOIN FETCH f.airline " +
           "JOIN FETCH f.aircraft " +
           "JOIN FETCH f.departureAirport " +
           "JOIN FETCH f.arrivalAirport " +
           "WHERE f.flightId = :id")
    Flight findByIdWithDetails(@Param("id") Long id);

    List<Flight> findByStatus(FlightStatus status);
}
