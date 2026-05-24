package com.skyjet.backend.controller;

import com.skyjet.backend.dto.BookingDTO;
import com.skyjet.backend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingDTO> myBookings(Authentication authentication) {
        return bookingService.getBookingsForUser(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDTO createBooking(Authentication authentication,
                                    @Valid @RequestBody BookingDTO request) {
        return bookingService.createBooking(authentication.getName(), request);
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingDTO cancelBooking(Authentication authentication,
                                    @PathVariable String bookingId) {
        return bookingService.cancelBooking(authentication.getName(), bookingId);
    }
}
