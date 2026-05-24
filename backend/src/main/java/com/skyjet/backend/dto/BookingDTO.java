package com.skyjet.backend.dto;

import com.skyjet.backend.entity.Booking;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private String bookingId;

    @NotNull
    private Long flightId;

    private String flightNumber;
    private String route;
    private String schedule;
    private String seatNumber;

    @NotBlank
    private String passengerName;

    private String status;
    private BigDecimal totalCost;
    private LocalDateTime bookingDate;

    public static BookingDTO fromEntity(Booking booking) {
        String route = booking.getFlight().getOrigin() + " -> " + booking.getFlight().getDestination();
        String schedule = booking.getFlight().getDepartureTime() + " - " + booking.getFlight().getArrivalTime();

        return BookingDTO.builder()
                .id(booking.getId())
                .bookingId(booking.getBookingId())
                .flightId(booking.getFlight().getId())
                .flightNumber(booking.getFlight().getFlightNumber())
                .route(route)
                .schedule(schedule)
                .seatNumber(booking.getSeatNumber())
                .passengerName(booking.getPassengerName())
                .status(booking.getStatus())
                .totalCost(booking.getTotalCost())
                .bookingDate(booking.getBookingDate())
                .build();
    }
}
