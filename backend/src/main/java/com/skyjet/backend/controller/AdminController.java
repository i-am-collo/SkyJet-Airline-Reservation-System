package com.skyjet.backend.controller;

import com.skyjet.backend.dto.*;
import com.skyjet.backend.service.AuditService;
import com.skyjet.backend.service.BookingService;
import com.skyjet.backend.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final FlightService flightService;
    private final BookingService bookingService;
    private final AuditService auditService;

    public AdminController(FlightService flightService,
                           BookingService bookingService,
                           AuditService auditService) {
        this.flightService = flightService;
        this.bookingService = bookingService;
        this.auditService = auditService;
    }

    // ---- Flight CRUD ----

    @PostMapping("/flights")
    @ResponseStatus(HttpStatus.CREATED)
    public FlightDTO createFlight(Authentication authentication,
                                  @Valid @RequestBody FlightDTO request) {
        return flightService.createFlight(request, authentication.getName());
    }

    @PutMapping("/flights/{id}")
    public FlightDTO updateFlight(Authentication authentication,
                                  @PathVariable Long id,
                                  @Valid @RequestBody FlightDTO request) {
        return flightService.updateFlight(id, request, authentication.getName());
    }

    @DeleteMapping("/flights/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlight(Authentication authentication,
                             @PathVariable Long id) {
        flightService.deleteFlight(id, authentication.getName());
    }

    // ---- Booking Management ----

    @GetMapping("/bookings")
    public List<BookingDTO> allBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping("/bookings/{bookingRef}/cancel")
    public BookingDTO adminCancelBooking(Authentication authentication,
                                         @PathVariable String bookingRef) {
        return bookingService.adminCancelBooking(authentication.getName(), bookingRef);
    }

    // ---- Audit Logs ----

    @GetMapping("/audits")
    public List<LoginAuditDTO> getLoginAudits() {
        return auditService.getRecentLoginAudits().stream()
                .map(LoginAuditDTO::fromEntity)
                .toList();
    }

    @GetMapping("/audit-logs")
    public List<AuditLogDTO> getAuditLogs() {
        return auditService.getRecentAuditLogs().stream()
                .map(AuditLogDTO::fromEntity)
                .toList();
    }
}
