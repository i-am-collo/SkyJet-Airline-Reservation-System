-- V3__seed_real_data.sql
-- Clear old mock data
DELETE FROM bookings;
DELETE FROM flights;

-- Seed realistic international flight data
INSERT INTO flights (flight_number, airline, origin, destination, departure_time, arrival_time, duration, price, available_seats, aircraft, status) VALUES
('EK-722', 'Emirates', 'Nairobi (NBO)', 'Dubai (DXB)', '22:45', '04:50', '5h 05m', 480.00, 250, 'Boeing 777-300ER', 'ON TIME'),
('EK-719', 'Emirates', 'Dubai (DXB)', 'Nairobi (NBO)', '10:05', '14:15', '5h 10m', 460.00, 180, 'Boeing 777-300ER', 'ON TIME'),
('KQ-100', 'Kenya Airways', 'Nairobi (NBO)', 'London (LHR)', '09:15', '16:10', '8h 55m', 850.00, 234, 'Boeing 787-8 Dreamliner', 'ON TIME'),
('KQ-101', 'Kenya Airways', 'London (LHR)', 'Nairobi (NBO)', '18:25', '05:00', '8h 35m', 820.00, 201, 'Boeing 787-8 Dreamliner', 'ON TIME'),
('BA-65', 'British Airways', 'London (LHR)', 'Nairobi (NBO)', '10:10', '20:50', '8h 40m', 910.00, 150, 'Airbus A350-1000', 'DELAYED'),
('BA-64', 'British Airways', 'Nairobi (NBO)', 'London (LHR)', '23:10', '06:05', '8h 55m', 895.00, 120, 'Airbus A350-1000', 'ON TIME'),
('QR-1335', 'Qatar Airways', 'Doha (DOH)', 'Nairobi (NBO)', '01:50', '07:20', '5h 30m', 510.00, 190, 'Airbus A330-200', 'ON TIME'),
('QR-1336', 'Qatar Airways', 'Nairobi (NBO)', 'Doha (DOH)', '17:55', '23:10', '5h 15m', 525.00, 210, 'Airbus A330-200', 'ON TIME'),
('SA-184', 'South African Airways', 'Johannesburg (JNB)', 'Nairobi (NBO)', '10:00', '15:10', '4h 10m', 350.00, 140, 'Airbus A320-200', 'ON TIME'),
('SA-185', 'South African Airways', 'Nairobi (NBO)', 'Johannesburg (JNB)', '16:00', '19:15', '4h 15m', 345.00, 135, 'Airbus A320-200', 'ON TIME'),
('ET-302', 'Ethiopian Airlines', 'Addis Ababa (ADD)', 'Nairobi (NBO)', '08:15', '10:25', '2h 10m', 220.00, 80, 'Boeing 737 MAX 8', 'ON TIME'),
('ET-303', 'Ethiopian Airlines', 'Nairobi (NBO)', 'Addis Ababa (ADD)', '11:15', '13:20', '2h 05m', 215.00, 75, 'Boeing 737 MAX 8', 'ON TIME'),
('TK-607', 'Turkish Airlines', 'Istanbul (IST)', 'Nairobi (NBO)', '20:55', '03:30', '6h 35m', 650.00, 260, 'Boeing 777-300ER', 'ON TIME'),
('TK-608', 'Turkish Airlines', 'Nairobi (NBO)', 'Istanbul (IST)', '05:00', '11:45', '6h 45m', 660.00, 240, 'Boeing 777-300ER', 'ON TIME'),
('AF-814', 'Air France', 'Paris (CDG)', 'Nairobi (NBO)', '10:25', '20:10', '8h 45m', 880.00, 210, 'Boeing 787-9 Dreamliner', 'DELAYED');
