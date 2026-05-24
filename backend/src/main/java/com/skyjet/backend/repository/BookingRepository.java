package com.skyjet.backend.repository;

import com.skyjet.backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BookingRepository - Data access layer for Booking entities
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingId(String bookingId);

    List<Booking> findByUser_Id(Long userId);

    List<Booking> findByFlight_Id(Long flightId);

    List<Booking> findByStatus(String status);

    List<Booking> findByUser_IdAndStatus(Long userId, String status);

    boolean existsByFlight_IdAndSeatNumberAndStatus(Long flightId, String seatNumber, String status);
}
