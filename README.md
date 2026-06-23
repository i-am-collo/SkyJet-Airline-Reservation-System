# SkyJet Airline Reservation System

SkyJet is a JavaFX desktop airline reservation app connected to a Spring Boot REST API backend with PostgreSQL persistence (hosted on Supabase). The frontend handles the user experience, while the backend owns authentication, flight data, bookings, admin flight management, JWT security, and database schema.

## Architecture

```text
JavaFX Desktop App
  src/main/java/app
  src/main/java/controllers
  src/main/java/models
  src/main/java/services/ApiClient.java
        |
        | HTTP + JSON
        | Authorization: Bearer <JWT>
        v
Spring Boot Backend
  backend/src/main/java/com/skyjet/backend
        |
        | JPA (Hibernate)
        v
PostgreSQL (Supabase)
```

The JavaFX app calls the backend at `http://localhost:8080` by default. You can override this with:

```bash
mvn javafx:run -Dskyjet.api.baseUrl=http://localhost:8080
```

## Features

| Area | Status |
|------|--------|
| Login and registration | Backend connected via `/api/auth` |
| JWT session handling | Token stored in JavaFX session manager |
| Flight search | Backend connected via `/api/flights` |
| Booking creation | Backend connected via `/api/bookings` |
| Booking history | Loaded from backend |
| Admin flight CRUD | Backend connected via `/api/admin/flights` |
| PostgreSQL (Supabase) | Schema managed in Supabase |
| Seat map | UI still uses a local hard-coded booked-seat map |
| Booking cancellation UI | Backend endpoint exists; UI action not added yet |

## Prerequisites

| Tool | Recommended |
|------|-------------|
| JDK | 17 or newer |
| Maven | 3.8+ |
| JavaFX | Pulled by Maven dependencies |

## Quick Start

### 1. Configure the database

Copy the example config and fill in your Supabase credentials:

```bash
cd backend/src/main/resources
cp application.properties.example application.properties
```

Edit `application.properties` with your Supabase connection details:

```properties
spring.datasource.url=jdbc:postgresql://db.<YOUR_PROJECT_REF>.supabase.co:5432/postgres?sslmode=require
spring.datasource.username=postgres
spring.datasource.password=<YOUR_SUPABASE_PASSWORD>
```

### 2. Run the backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts at:

```text
http://localhost:8080
```

### 3. Run the JavaFX frontend

From the project root:

```bash
mvn javafx:run
```

The frontend will call the backend at `http://localhost:8080`.

## Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| User | `james@skyjet.com` | `password123` |
| Admin | `admin@skyjet.com` | `admin123` |

## API Smoke Tests

After the backend is running, you can verify the API.

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@skyjet.com\",\"password\":\"admin123\"}"
```

Copy the returned `token` for protected endpoints.

### List flights

```bash
curl http://localhost:8080/api/flights
```

### Search flights

```bash
curl "http://localhost:8080/api/flights?origin=Nairobi&destination=London"
```

### List current user's bookings

```bash
curl http://localhost:8080/api/bookings \
  -H "Authorization: Bearer <TOKEN>"
```

### Create a booking

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"flightId\":1,\"seatNumber\":\"12A\",\"passengerName\":\"James Smith\",\"totalCost\":986.00}"
```

### Admin create flight

```bash
curl -X POST http://localhost:8080/api/admin/flights \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"flightNumber\":\"SJ-222\",\"airline\":\"SkyJet\",\"aircraft\":\"Boeing 737\",\"origin\":\"Nairobi (NBO)\",\"destination\":\"Kigali (KGL)\",\"departureTime\":\"10:00\",\"arrivalTime\":\"11:30\",\"duration\":\"1h 30m\",\"price\":180.00,\"availableSeats\":80,\"status\":\"ON TIME\"}"
```

## Project Structure

```text
SkyJet-AirlineReservationSystem/
  pom.xml                         JavaFX frontend build
  src/main/java/
    app/Main.java                 JavaFX entry point
    controllers/                  JavaFX screen controllers
    models/                       JavaFX models and session state
    services/ApiClient.java       HTTP client for backend API
    module-info.java
  src/main/resources/
    views/                        FXML screens
    styles/style.css
  backend/
    pom.xml                       Spring Boot backend build
    src/main/java/com/skyjet/backend/
      config/                     Security and CORS
      controller/                 REST controllers
      dto/                        Request/response DTOs
      entity/                     JPA entities
      exception/                  API error handling
      repository/                 Spring Data repositories
      security/                   JWT provider/filter
      service/                    Business logic
    src/main/resources/
      application.properties      Configuration (gitignored)
      application.properties.example  Template for configuration
      db/seed_demo_data.sql       Seed data SQL script
```

## Backend Configuration

Main backend settings live in:

```text
backend/src/main/resources/application.properties
```

Important values:

```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://db.<PROJECT_REF>.supabase.co:5432/postgres?sslmode=require
spring.datasource.username=postgres
spring.datasource.password=<YOUR_PASSWORD>
app.jwt.secret=your-very-secure-secret-key-change-this-in-production-at-least-32-characters-long!
app.jwt.expiration=86400000
app.cors.allowed-origins=http://localhost:3000,http://localhost:8080
```

For production, change `app.jwt.secret`.

## Frontend Configuration

The frontend API base URL defaults to:

```text
http://localhost:8080
```

Override it with a JVM system property:

```bash
mvn javafx:run -Dskyjet.api.baseUrl=http://localhost:8081
```

## Troubleshooting

### Backend cannot connect to database

Make sure your `application.properties` has the correct Supabase credentials. Check that your Supabase project is active (not paused).

### Frontend login fails

Make sure:

- Backend is running on `http://localhost:8080`.
- You can call `http://localhost:8080/api/flights`.
- The frontend `skyjet.api.baseUrl` points to the backend.
- Demo users exist in the database (run `seed_demo_data.sql` if needed).

## Remaining Work

- Load booked seats from backend instead of using the local hard-coded seat map.
- Add booking cancellation controls to the JavaFX booking history.
- Optionally show admin booking and login audit views.
- Add backend service/controller tests and frontend integration checks.
