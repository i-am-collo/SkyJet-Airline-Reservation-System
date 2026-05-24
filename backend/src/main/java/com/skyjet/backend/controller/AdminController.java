package com.skyjet.backend.controller;

import com.skyjet.backend.dto.BookingDTO;
import com.skyjet.backend.dto.FlightDTO;
import com.skyjet.backend.entity.LoginAudit;
import com.skyjet.backend.service.AuditService;
import com.skyjet.backend.service.BookingService;
import com.skyjet.backend.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/flights")
    @ResponseStatus(HttpStatus.CREATED)
    public FlightDTO createFlight(@Valid @RequestBody FlightDTO request) {
        return flightService.createFlight(request);
    }

    @PutMapping("/flights/{id}")
    public FlightDTO updateFlight(@PathVariable Long id,
                                  @Valid @RequestBody FlightDTO request) {
        return flightService.updateFlight(id, request);
    }

    @DeleteMapping("/flights/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
    }

    @GetMapping("/bookings")
    public List<BookingDTO> allBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/audits")
    public List<LoginAudit> loginAudits() {
        return auditService.getLoginAudits();
    }
}
