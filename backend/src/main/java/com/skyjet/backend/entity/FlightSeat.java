package com.skyjet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * FlightSeat Entity - Maps to the flight_seats table
 * Represents a specific seat on a specific flight and its availability
 */
@Entity
@Table(name = "flight_seats", uniqueConstraints = {
    @UniqueConstraint(name = "unique_flight_seat", columnNames = {"flight_id", "seat_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flight_seat_id")
    private Long flightSeatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "price", precision = 10, scale = 2)
    @Builder.Default
    private java.math.BigDecimal price = java.math.BigDecimal.ZERO;
}
