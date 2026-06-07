package com.skyjet.backend.service;

import com.skyjet.backend.dto.BookingDTO;
import com.skyjet.backend.entity.*;
import com.skyjet.backend.entity.enums.BookingStatus;
import com.skyjet.backend.entity.enums.FlightStatus;
import com.skyjet.backend.entity.enums.SeatClass;
import com.skyjet.backend.exception.ApiException;
import com.skyjet.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final FlightSeatRepository flightSeatRepository;
    private final PassengerRepository passengerRepository;
    private final TicketRepository ticketRepository;
    private final AuditService auditService;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          FlightRepository flightRepository,
                          FlightSeatRepository flightSeatRepository,
                          PassengerRepository passengerRepository,
                          TicketRepository ticketRepository,
                          AuditService auditService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.flightRepository = flightRepository;
        this.flightSeatRepository = flightSeatRepository;
        this.passengerRepository = passengerRepository;
        this.ticketRepository = ticketRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsForUser(String email) {
        User user = currentUser(email);
        return bookingRepository.findByUser_UserId(user.getUserId()).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingByRef(String email, String bookingRef) {
        User user = currentUser(email);
        Booking booking = bookingRepository.findByBookingRefWithUser(bookingRef)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getUserId().equals(user.getUserId()) && !user.isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to this booking");
        }

        return toDTO(booking);
    }

    @Transactional(readOnly = true)
    public List<String> getConfirmedSeatNumbersForFlight(Long flightId) {
        List<FlightSeat> bookedSeats = flightSeatRepository
                .findByFlight_FlightIdAndIsAvailable(flightId, false);
        return bookedSeats.stream()
                .map(fs -> fs.getSeat().getSeatNumber())
                .toList();
    }

    @Transactional
    public BookingDTO createBooking(String email, BookingDTO request) {
        User user = currentUser(email);

        Flight flight = flightRepository.findByIdWithDetails(request.getFlightId());
        if (flight == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Flight not found");
        }

        // Validate flight is still bookable
        if (flight.getStatus() == FlightStatus.CANCELLED ||
            flight.getStatus() == FlightStatus.DEPARTED ||
            flight.getStatus() == FlightStatus.ARRIVED) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot book a flight with status: " + flight.getStatus().name());
        }

        // Check seat availability
        long availableCount = flightSeatRepository.countAvailableSeats(flight.getFlightId());
        if (availableCount <= 0) {
            throw new ApiException(HttpStatus.CONFLICT, "No seats available for this flight");
        }

        // Create booking
        BigDecimal total = request.getTotalCost() != null ? request.getTotalCost() : BigDecimal.ZERO;
        Booking booking = Booking.builder()
                .user(user)
                .bookingRef(nextBookingRef())
                .status(BookingStatus.CONFIRMED)
                .totalAmount(total)
                .build();
        booking = bookingRepository.save(booking);

        // Split passenger name into first/last
        String[] names = splitName(request.getPassengerName());
        Passenger passenger = Passenger.builder()
                .booking(booking)
                .firstName(names[0])
                .lastName(names[1])
                .passportNo(request.getPassportNo() != null ? request.getPassportNo() : "N/A")
                .build();
        passenger = passengerRepository.save(passenger);

        // Find and reserve a flight seat
        FlightSeat flightSeat = null;
        if (request.getSeatNumber() != null && !request.getSeatNumber().isBlank()) {
            List<FlightSeat> available = flightSeatRepository.findAvailableSeatsForFlight(flight.getFlightId());
            flightSeat = available.stream()
                    .filter(fs -> fs.getSeat().getSeatNumber().equalsIgnoreCase(request.getSeatNumber()))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "Seat is already booked"));
        } else {
            List<FlightSeat> available = flightSeatRepository.findAvailableSeatsForFlight(flight.getFlightId());
            if (!available.isEmpty()) {
                flightSeat = available.get(0);
            }
        }

        // Mark seat as unavailable
        if (flightSeat != null) {
            flightSeat.setIsAvailable(false);
            flightSeatRepository.save(flightSeat);
        }

        // Determine seat class
        SeatClass ticketClass = SeatClass.ECONOMY;
        if (flightSeat != null && flightSeat.getSeat() != null) {
            ticketClass = flightSeat.getSeat().getSeatClass();
        }

        // Create ticket
        Ticket ticket = Ticket.builder()
                .booking(booking)
                .passenger(passenger)
                .flight(flight)
                .flightSeat(flightSeat)
                .ticketNumber("TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .ticketClass(ticketClass)
                .build();
        ticketRepository.save(ticket);

        // Audit
        auditService.logAction(email, "CREATE", "BOOKING", booking.getBookingId(),
                "Created booking " + booking.getBookingRef() + " for flight " + flight.getFlightId());

        return toDTO(booking, ticket, passenger, flight, flightSeat);
    }

    @Transactional
    public BookingDTO cancelBooking(String email, String bookingRef) {
        User user = currentUser(email);
        Booking booking = bookingRepository.findByBookingRefWithUser(bookingRef)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getUserId().equals(user.getUserId()) && !user.isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, "Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release all flight seats for this booking's tickets
        List<Ticket> tickets = ticketRepository.findByBooking_BookingId(booking.getBookingId());
        for (Ticket ticket : tickets) {
            if (ticket.getFlightSeat() != null) {
                FlightSeat fs = ticket.getFlightSeat();
                fs.setIsAvailable(true);
                flightSeatRepository.save(fs);
            }
        }

        // Audit
        auditService.logAction(email, "CANCEL", "BOOKING", booking.getBookingId(),
                "Cancelled booking " + booking.getBookingRef());

        return toDTO(booking);
    }

    /**
     * Admin cancel — allows cancelling any booking regardless of ownership.
     */
    @Transactional
    public BookingDTO adminCancelBooking(String adminEmail, String bookingRef) {
        Booking booking = bookingRepository.findByBookingRefWithUser(bookingRef)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, "Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        List<Ticket> tickets = ticketRepository.findByBooking_BookingId(booking.getBookingId());
        for (Ticket ticket : tickets) {
            if (ticket.getFlightSeat() != null) {
                FlightSeat fs = ticket.getFlightSeat();
                fs.setIsAvailable(true);
                flightSeatRepository.save(fs);
            }
        }

        auditService.logAction(adminEmail, "CANCEL", "BOOKING", booking.getBookingId(),
                "Admin cancelled booking " + booking.getBookingRef());

        return toDTO(booking);
    }

    // ---- Private helpers ----

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private String nextBookingRef() {
        String ref;
        do {
            ref = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (bookingRepository.findByBookingRef(ref).isPresent());
        return ref;
    }

    private BookingDTO toDTO(Booking booking) {
        List<Ticket> tickets = ticketRepository.findByBookingIdWithDetails(booking.getBookingId());
        if (tickets.isEmpty()) {
            return BookingDTO.builder()
                    .id(booking.getBookingId())
                    .bookingId(booking.getBookingRef())
                    .status(booking.getStatus().name())
                    .totalCost(booking.getTotalAmount())
                    .bookingDate(booking.getBookingDate())
                    .passengerName("")
                    .flightNumber("")
                    .route("")
                    .schedule("")
                    .seatNumber("")
                    .build();
        }

        Ticket firstTicket = tickets.get(0);
        return toDTO(booking, firstTicket, firstTicket.getPassenger(),
                firstTicket.getFlight(), firstTicket.getFlightSeat());
    }

    private BookingDTO toDTO(Booking booking, Ticket ticket, Passenger passenger,
                             Flight flight, FlightSeat flightSeat) {
        String airlineIata = flight.getAirline() != null ? flight.getAirline().getIataCode() : "";
        String flightNumber = airlineIata + "-" + flight.getFlightId();

        String depCity = flight.getDepartureAirport() != null ?
                flight.getDepartureAirport().getCity() + " (" + flight.getDepartureAirport().getIataCode() + ")" : "";
        String arrCity = flight.getArrivalAirport() != null ?
                flight.getArrivalAirport().getCity() + " (" + flight.getArrivalAirport().getIataCode() + ")" : "";
        String route = depCity + " -> " + arrCity;

        String depTime = flight.getDepartureTime() != null ? flight.getDepartureTime().format(TIME_FMT) : "";
        String arrTime = flight.getArrivalTime() != null ? flight.getArrivalTime().format(TIME_FMT) : "";
        String schedule = depTime + " - " + arrTime;

        String seatNumber = "";
        if (flightSeat != null && flightSeat.getSeat() != null) {
            seatNumber = flightSeat.getSeat().getSeatNumber();
        }

        String passengerName = "";
        if (passenger != null) {
            passengerName = (passenger.getFirstName() + " " + passenger.getLastName()).trim();
        }

        return BookingDTO.builder()
                .id(booking.getBookingId())
                .bookingId(booking.getBookingRef())
                .flightId(flight.getFlightId())
                .flightNumber(flightNumber)
                .route(route)
                .schedule(schedule)
                .seatNumber(seatNumber)
                .passengerName(passengerName)
                .status(booking.getStatus().name())
                .totalCost(booking.getTotalAmount())
                .bookingDate(booking.getBookingDate())
                .build();
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{"", ""};
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return new String[]{parts[0], parts[1]};
    }
}
