package com.skyjet.backend.dto;

import com.skyjet.backend.entity.Flight;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private Long id;

    @NotBlank
    private String flightNumber;

    @NotBlank
    private String airline;

    private String aircraft;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @NotBlank
    private String departureTime;

    private String arrivalTime;
    private String duration;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer availableSeats;

    private String status;

    public static FlightDTO fromEntity(Flight flight) {
        return FlightDTO.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .aircraft(flight.getAircraft())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .duration(flight.getDuration())
                .price(flight.getPrice())
                .availableSeats(flight.getAvailableSeats())
                .status(flight.getStatus())
                .build();
    }

    public Flight toEntity() {
        return Flight.builder()
                .flightNumber(flightNumber)
                .airline(airline)
                .aircraft(aircraft)
                .origin(origin)
                .destination(destination)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .duration(duration)
                .price(price)
                .availableSeats(availableSeats)
                .status(status == null || status.isBlank() ? "ON TIME" : status)
                .build();
    }
}
