-- V1__initial_schema.sql
-- SkyJet Database Initial Schema
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    -- USER, ADMIN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Flights table
CREATE TABLE flights (
    id BIGSERIAL PRIMARY KEY,
    flight_number VARCHAR(50) UNIQUE NOT NULL,
    airline VARCHAR(100) NOT NULL,
    aircraft VARCHAR(100),
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_time VARCHAR(10) NOT NULL,
    arrival_time VARCHAR(10),
    duration VARCHAR(50),
    price DECIMAL(10, 2) NOT NULL,
    available_seats INT NOT NULL DEFAULT 0,
    status VARCHAR(50) DEFAULT 'ON TIME',
    -- ON TIME, DELAYED, CANCELLED, BOARDING
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Bookings table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_id VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    flight_id BIGINT NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
    seat_number VARCHAR(10),
    passenger_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'CONFIRMED',
    -- CONFIRMED, CANCELLED, COMPLETED
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_cost DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Login audit table
CREATE TABLE login_audits (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    -- SUCCESS, FAILURE
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_flights_number ON flights(flight_number);
CREATE INDEX idx_flights_origin_destination ON flights(origin, destination);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_flight_id ON bookings(flight_id);
CREATE INDEX idx_login_audits_email ON login_audits(email);
CREATE INDEX idx_login_audits_time ON login_audits(login_time);
-- Seed demo admin user (password: admin123 hashed with BCrypt)
INSERT INTO users (user_id, name, email, password, role)
VALUES (
        'U002',
        'Admin User',
        'admin@skyjet.com',
        '$2a$10$quJv4yincJtldL93qcc8Y.gaCIKlFANpQ.YB9kK24FgnPe7eMtRey',
        'ADMIN'
    );
-- Seed demo regular user (password: password123 hashed with BCrypt)
INSERT INTO users (user_id, name, email, password, role)
VALUES (
        'U001',
        'James Smith',
        'james@skyjet.com',
        '$2a$10$19moy27Wjs0YyEZ6Dm3lNeBFyWjGTouSA19Tq6a9yalbsKHVeJW46',
        'USER'
    );
