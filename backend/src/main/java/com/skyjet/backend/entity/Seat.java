package com.skyjet.backend.entity;

import com.skyjet.backend.entity.enums.SeatClass;
import jakarta.persistence.*;
import lombok.*;

/**
 * Seat Entity - Maps to the seats table
 */
@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(name = "unique_aircraft_seat", columnNames = {"aircraft_id", "seat_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, columnDefinition = "seat_class_enum")
    private SeatClass seatClass;
}
