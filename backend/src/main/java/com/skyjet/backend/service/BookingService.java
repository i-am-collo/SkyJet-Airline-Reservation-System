package com.skyjet.backend.service;

import com.skyjet.backend.dto.BookingDTO;
import com.skyjet.backend.entity.Booking;
import com.skyjet.backend.entity.Flight;
import com.skyjet.backend.entity.User;
import com.skyjet.backend.exception.ApiException;
import com.skyjet.backend.repository.BookingRepository;
import com.skyjet.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FlightService flightService;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          FlightService flightService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.flightService = flightService;
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsForUser(String email) {
        User user = currentUser(email);
        return bookingRepository.findByUser_Id(user.getId()).stream()
                .map(BookingDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream().map(BookingDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<String> getConfirmedSeatNumbersForFlight(Long flightId) {
        flightService.findEntity(flightId);
        return bookingRepository.findByFlight_IdAndStatus(flightId, "CONFIRMED").stream()
                .map(Booking::getSeatNumber)
                .filter(seat -> seat != null && !seat.isBlank())
                .toList();
    }

    @Transactional
    public BookingDTO createBooking(String email, BookingDTO request) {
        User user = currentUser(email);
        Flight flight = flightService.findEntity(request.getFlightId());

        if (flight.getAvailableSeats() <= 0) {
            throw new ApiException(HttpStatus.CONFLICT, "No seats available for this flight");
        }

        if (request.getSeatNumber() != null && !request.getSeatNumber().isBlank()
                && bookingRepository.existsByFlight_IdAndSeatNumberAndStatus(
                flight.getId(), request.getSeatNumber(), "CONFIRMED")) {
            throw new ApiException(HttpStatus.CONFLICT, "Seat is already booked");
        }

        BigDecimal total = request.getTotalCost() != null ? request.getTotalCost() : flight.getPrice();
        Booking booking = Booking.builder()
                .bookingId(nextBookingId())
                .user(user)
                .flight(flight)
                .seatNumber(request.getSeatNumber())
                .passengerName(request.getPassengerName())
                .status("CONFIRMED")
                .totalCost(total)
                .build();

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDTO cancelBooking(String email, String bookingId) {
        User user = currentUser(email);
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot cancel this booking");
        }

        if (!"CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            booking.setStatus("CANCELLED");
            booking.getFlight().setAvailableSeats(booking.getFlight().getAvailableSeats() + 1);
        }
        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private String nextBookingId() {
        String bookingId;
        do {
            bookingId = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (bookingRepository.findByBookingId(bookingId).isPresent());
        return bookingId;
    }
}
