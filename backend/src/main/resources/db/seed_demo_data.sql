-- seed_demo_data.sql
-- Run this manually against your Supabase database to populate demo data.
-- This is NOT a Flyway migration — just a standalone seed script.

-- ============================================================
-- AUDIT TABLES (create if they don't exist)
-- ============================================================
CREATE TABLE IF NOT EXISTS login_audits (
    audit_id     BIGSERIAL PRIMARY KEY,
    user_id      BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    email        VARCHAR(255) NOT NULL,
    success      BOOLEAN NOT NULL,
    reason       VARCHAR(255),
    ip_address   VARCHAR(45),
    user_agent   TEXT,
    login_time   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_logs (
    log_id       BIGSERIAL PRIMARY KEY,
    user_id      BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    action       VARCHAR(50) NOT NULL,
    entity_type  VARCHAR(50) NOT NULL,
    entity_id    BIGINT,
    description  TEXT,
    created_at   TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- AIRLINES
-- ============================================================
INSERT INTO airlines (name, iata_code) VALUES
('Kenya Airways', 'KQ'),
('Emirates', 'EK'),
('British Airways', 'BA'),
('Qatar Airways', 'QR'),
('South African Airways', 'SA'),
('Ethiopian Airlines', 'ET'),
('Turkish Airlines', 'TK'),
('Air France', 'AF')
ON CONFLICT (iata_code) DO NOTHING;

-- ============================================================
-- AIRPORTS
-- ============================================================
INSERT INTO airports (name, iata_code, city, country) VALUES
('Jomo Kenyatta International Airport', 'NBO', 'Nairobi', 'Kenya'),
('Dubai International Airport', 'DXB', 'Dubai', 'United Arab Emirates'),
('Heathrow Airport', 'LHR', 'London', 'United Kingdom'),
('Hamad International Airport', 'DOH', 'Doha', 'Qatar'),
('O.R. Tambo International Airport', 'JNB', 'Johannesburg', 'South Africa'),
('Bole International Airport', 'ADD', 'Addis Ababa', 'Ethiopia'),
('Istanbul Airport', 'IST', 'Istanbul', 'Turkey'),
('Charles de Gaulle Airport', 'CDG', 'Paris', 'France'),
('John F. Kennedy International Airport', 'JFK', 'New York', 'United States'),
('Changi Airport', 'SIN', 'Singapore', 'Singapore'),
('Narita International Airport', 'NRT', 'Tokyo', 'Japan'),
('Suvarnabhumi Airport', 'BKK', 'Bangkok', 'Thailand'),
('Chhatrapati Shivaji Maharaj International Airport', 'BOM', 'Mumbai', 'India'),
('Berlin Brandenburg Airport', 'BER', 'Berlin', 'Germany')
ON CONFLICT (iata_code) DO NOTHING;

-- ============================================================
-- AIRCRAFT (linked to airlines)
-- ============================================================
INSERT INTO aircraft (airline_id, model, registration_number, capacity) VALUES
((SELECT airline_id FROM airlines WHERE iata_code = 'KQ'), 'Boeing 787-8 Dreamliner', '5Y-KZA', 234),
((SELECT airline_id FROM airlines WHERE iata_code = 'EK'), 'Boeing 777-300ER', 'A6-ECA', 396),
((SELECT airline_id FROM airlines WHERE iata_code = 'BA'), 'Airbus A350-1000', 'G-XWBA', 331),
((SELECT airline_id FROM airlines WHERE iata_code = 'QR'), 'Airbus A330-200', 'A7-ACA', 252),
((SELECT airline_id FROM airlines WHERE iata_code = 'SA'), 'Airbus A320-200', 'ZS-SZA', 150),
((SELECT airline_id FROM airlines WHERE iata_code = 'ET'), 'Boeing 737 MAX 8', 'ET-AVJ', 160),
((SELECT airline_id FROM airlines WHERE iata_code = 'TK'), 'Boeing 777-300ER', 'TC-JJA', 370),
((SELECT airline_id FROM airlines WHERE iata_code = 'AF'), 'Boeing 787-9 Dreamliner', 'F-HRBA', 276)
ON CONFLICT (registration_number) DO NOTHING;

-- ============================================================
-- SEATS (sample seats for each aircraft - Economy rows 10-30, Business 4-9, First 1-3)
-- ============================================================
-- For simplicity, we'll generate seats for the first aircraft (KQ 787)
DO $$
DECLARE
    ac_id BIGINT;
    row_num INT;
    col CHAR(1);
BEGIN
    SELECT aircraft_id INTO ac_id FROM aircraft WHERE registration_number = '5Y-KZA';
    IF ac_id IS NOT NULL THEN
        -- First Class: rows 1-3, seats A-D
        FOR row_num IN 1..3 LOOP
            FOREACH col IN ARRAY ARRAY['A','B','C','D'] LOOP
                INSERT INTO seats (aircraft_id, seat_number, seat_class)
                VALUES (ac_id, row_num || col, 'FIRST_CLASS')
                ON CONFLICT (aircraft_id, seat_number) DO NOTHING;
            END LOOP;
        END LOOP;
        -- Business: rows 4-9, seats A-F
        FOR row_num IN 4..9 LOOP
            FOREACH col IN ARRAY ARRAY['A','B','C','D','E','F'] LOOP
                INSERT INTO seats (aircraft_id, seat_number, seat_class)
                VALUES (ac_id, row_num || col, 'BUSINESS')
                ON CONFLICT (aircraft_id, seat_number) DO NOTHING;
            END LOOP;
        END LOOP;
        -- Economy: rows 10-30, seats A-F
        FOR row_num IN 10..30 LOOP
            FOREACH col IN ARRAY ARRAY['A','B','C','D','E','F'] LOOP
                INSERT INTO seats (aircraft_id, seat_number, seat_class)
                VALUES (ac_id, row_num || col, 'ECONOMY')
                ON CONFLICT (aircraft_id, seat_number) DO NOTHING;
            END LOOP;
        END LOOP;
    END IF;
END $$;

-- Repeat for Emirates 777 (A6-ECA)
DO $$
DECLARE
    ac_id BIGINT;
    row_num INT;
    col CHAR(1);
BEGIN
    SELECT aircraft_id INTO ac_id FROM aircraft WHERE registration_number = 'A6-ECA';
    IF ac_id IS NOT NULL THEN
        FOR row_num IN 1..3 LOOP
            FOREACH col IN ARRAY ARRAY['A','B','C','D'] LOOP
                INSERT INTO seats (aircraft_id, seat_number, seat_class)
                VALUES (ac_id, row_num || col, 'FIRST_CLASS')
                ON CONFLICT (aircraft_id, seat_number) DO NOTHING;
            END LOOP;
        END LOOP;
        FOR row_num IN 4..9 LOOP
            FOREACH col IN ARRAY ARRAY['A','B','C','D','E','F'] LOOP
                INSERT INTO seats (aircraft_id, seat_number, seat_class)
                VALUES (ac_id, row_num || col, 'BUSINESS')
                ON CONFLICT (aircraft_id, seat_number) DO NOTHING;
            END LOOP;
        END LOOP;
        FOR row_num IN 10..30 LOOP
            FOREACH col IN ARRAY ARRAY['A','B','C','D','E','F'] LOOP
                INSERT INTO seats (aircraft_id, seat_number, seat_class)
                VALUES (ac_id, row_num || col, 'ECONOMY')
                ON CONFLICT (aircraft_id, seat_number) DO NOTHING;
            END LOOP;
        END LOOP;
    END IF;
END $$;

-- ============================================================
-- FLIGHTS
-- ============================================================
INSERT INTO flights (airline_id, aircraft_id, departure_airport_id, arrival_airport_id, departure_time, arrival_time, status) VALUES
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'EK'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = 'A6-ECA'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    (SELECT airport_id FROM airports WHERE iata_code = 'DXB'),
    '2026-07-01 22:45:00', '2026-07-02 04:50:00', 'SCHEDULED'
),
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'EK'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = 'A6-ECA'),
    (SELECT airport_id FROM airports WHERE iata_code = 'DXB'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    '2026-07-02 10:05:00', '2026-07-02 14:15:00', 'SCHEDULED'
),
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'KQ'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = '5Y-KZA'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    (SELECT airport_id FROM airports WHERE iata_code = 'LHR'),
    '2026-07-01 09:15:00', '2026-07-01 16:10:00', 'SCHEDULED'
),
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'KQ'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = '5Y-KZA'),
    (SELECT airport_id FROM airports WHERE iata_code = 'LHR'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    '2026-07-02 18:25:00', '2026-07-03 05:00:00', 'SCHEDULED'
),
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'BA'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = 'G-XWBA'),
    (SELECT airport_id FROM airports WHERE iata_code = 'LHR'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    '2026-07-01 10:10:00', '2026-07-01 20:50:00', 'DELAYED'
),
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'QR'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = 'A7-ACA'),
    (SELECT airport_id FROM airports WHERE iata_code = 'DOH'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    '2026-07-03 01:50:00', '2026-07-03 07:20:00', 'SCHEDULED'
),
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'SA'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = 'ZS-SZA'),
    (SELECT airport_id FROM airports WHERE iata_code = 'JNB'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    '2026-07-01 10:00:00', '2026-07-01 15:10:00', 'SCHEDULED'
),
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'ET'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = 'ET-AVJ'),
    (SELECT airport_id FROM airports WHERE iata_code = 'ADD'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    '2026-07-01 08:15:00', '2026-07-01 10:25:00', 'SCHEDULED'
),
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'TK'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = 'TC-JJA'),
    (SELECT airport_id FROM airports WHERE iata_code = 'IST'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    '2026-07-01 20:55:00', '2026-07-02 03:30:00', 'SCHEDULED'
),
(
    (SELECT airline_id FROM airlines WHERE iata_code = 'AF'),
    (SELECT aircraft_id FROM aircraft WHERE registration_number = 'F-HRBA'),
    (SELECT airport_id FROM airports WHERE iata_code = 'CDG'),
    (SELECT airport_id FROM airports WHERE iata_code = 'NBO'),
    '2026-07-01 10:25:00', '2026-07-01 20:10:00', 'DELAYED'
);

-- ============================================================
-- FLIGHT_SEATS (populate for each flight using the aircraft's seats)
-- ============================================================
INSERT INTO flight_seats (flight_id, seat_id, is_available)
SELECT f.flight_id, s.seat_id, TRUE
FROM flights f
JOIN aircraft a ON f.aircraft_id = a.aircraft_id
JOIN seats s ON s.aircraft_id = a.aircraft_id
ON CONFLICT (flight_id, seat_id) DO NOTHING;

-- ============================================================
-- DEMO USERS (password: admin123 and password123 hashed with BCrypt)
-- ============================================================
INSERT INTO users (first_name, last_name, email, password_hash, role) VALUES
('Admin', 'User', 'admin@skyjet.com', '$2a$10$quJv4yincJtldL93qcc8Y.gaCIKlFANpQ.YB9kK24FgnPe7eMtRey', 'ADMIN'),
('James', 'Smith', 'james@skyjet.com', '$2a$10$19moy27Wjs0YyEZ6Dm3lNeBFyWjGTouSA19Tq6a9yalbsKHVeJW46', 'CUSTOMER')
ON CONFLICT (email) DO UPDATE SET
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role;
