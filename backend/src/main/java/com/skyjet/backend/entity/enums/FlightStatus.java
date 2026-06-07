package com.skyjet.backend.entity.enums;

/**
 * Maps to Postgres flight_status_enum
 */
public enum FlightStatus {
    SCHEDULED,
    BOARDING,
    DELAYED,
    CANCELLED,
    DEPARTED,
    ARRIVED
}
