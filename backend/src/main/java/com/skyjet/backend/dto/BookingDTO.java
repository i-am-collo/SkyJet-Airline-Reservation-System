package com.skyjet.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BookingDTO - Preserves the same JSON shape as the old API for frontend compatibility.
 * Fields are assembled from booking + first ticket + first passenger.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private String bookingId;     // maps to booking_ref

    @NotNull
    private Long flightId;

    private String flightNumber;
    private String route;
    private String schedule;
    private String seatNumber;

    @NotBlank
    private String passengerName;

    private String passportNo;

    private String status;
    private BigDecimal totalCost;  // maps to total_amount
    private LocalDateTime bookingDate;
}
