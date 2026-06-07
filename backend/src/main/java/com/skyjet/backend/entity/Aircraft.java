package com.skyjet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Aircraft Entity - Maps to the aircraft table
 */
@Entity
@Table(name = "aircraft")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aircraft_id")
    private Long aircraftId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    private Airline airline;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "registration_number", unique = true, length = 50)
    private String registrationNumber;

    @Column(nullable = false)
    private Integer capacity;
}
