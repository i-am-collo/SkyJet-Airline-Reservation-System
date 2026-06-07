package com.skyjet.backend.repository;

import com.skyjet.backend.entity.FlightSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightSeatRepository extends JpaRepository<FlightSeat, Long> {

    List<FlightSeat> findByFlight_FlightId(Long flightId);

    List<FlightSeat> findByFlight_FlightIdAndIsAvailable(Long flightId, Boolean isAvailable);

    @Query("SELECT fs FROM FlightSeat fs JOIN FETCH fs.seat WHERE fs.flight.flightId = :flightId AND fs.isAvailable = true")
    List<FlightSeat> findAvailableSeatsForFlight(@Param("flightId") Long flightId);

    @Query("SELECT COUNT(fs) FROM FlightSeat fs WHERE fs.flight.flightId = :flightId AND fs.isAvailable = true")
    long countAvailableSeats(@Param("flightId") Long flightId);

    Optional<FlightSeat> findByFlight_FlightIdAndSeat_SeatId(Long flightId, Long seatId);

    @Query("SELECT MIN(fs.price) FROM FlightSeat fs WHERE fs.flight.flightId = :flightId AND fs.isAvailable = true AND fs.price > 0")
    java.math.BigDecimal findMinPriceForFlight(@Param("flightId") Long flightId);
}
