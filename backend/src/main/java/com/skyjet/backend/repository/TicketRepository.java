package com.skyjet.backend.repository;

import com.skyjet.backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByBooking_BookingId(Long bookingId);

    List<Ticket> findByFlight_FlightId(Long flightId);

    @Query("SELECT t FROM Ticket t " +
           "JOIN FETCH t.flight f " +
           "JOIN FETCH f.airline " +
           "JOIN FETCH f.departureAirport " +
           "JOIN FETCH f.arrivalAirport " +
           "JOIN FETCH f.aircraft " +
           "LEFT JOIN FETCH t.flightSeat fs " +
           "LEFT JOIN FETCH fs.seat " +
           "JOIN FETCH t.passenger " +
           "WHERE t.booking.bookingId = :bookingId")
    List<Ticket> findByBookingIdWithDetails(@Param("bookingId") Long bookingId);
}
