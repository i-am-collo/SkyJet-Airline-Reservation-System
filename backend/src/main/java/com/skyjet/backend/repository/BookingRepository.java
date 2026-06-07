package com.skyjet.backend.repository;

import com.skyjet.backend.entity.Booking;
import com.skyjet.backend.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BookingRepository - Data access layer for Booking entities
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingRef(String bookingRef);

    List<Booking> findByUser_UserId(Long userId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByUser_UserIdAndStatus(Long userId, BookingStatus status);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user WHERE b.bookingRef = :ref")
    Optional<Booking> findByBookingRefWithUser(@Param("ref") String bookingRef);
}
