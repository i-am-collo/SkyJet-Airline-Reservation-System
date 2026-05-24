# SkyJet Backend Setup - Phase 1

## Overview
This is the Spring Boot REST API backend for the SkyJet Airline Reservation System. It provides endpoints for authentication, flights, bookings, and admin functionality.

## Prerequisites
- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose (for PostgreSQL)

## Quick Start

### 1. Start PostgreSQL with Docker
```bash
cd /path/to/SkyJet-AirlineReservationSystem
docker-compose up -d
```

This will:
- Start PostgreSQL on port 5432
- Create database: `skyjet_db`
- Create user: `skyjet_user` with password `skyjet_password`
- Start pgAdmin on port 5050 (optional, for database management)

### 2. Build the Backend
```bash
cd backend
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

### 4. Verify Setup
```bash
# Health check
curl http://localhost:8080/actuator/health

# List all flights
curl http://localhost:8080/api/flights
```

## Database

### Migrations
Database migrations are handled automatically by Flyway. When the application starts, it will:
1. Create all tables (`users`, `flights`, `bookings`, `login_audits`)
2. Create indexes for performance
3. Seed demo data (admin user, demo flights)

### Demo Credentials
- **Admin User**: `admin@skyjet.com` / `admin123`
- **Regular User**: `james@skyjet.com` / `password123`

### Database Access
pgAdmin is available at `http://localhost:5050`
- Email: `admin@skyjet.com`
- Password: `admin123`

## Configuration

### application.properties
Located at: `backend/src/main/resources/application.properties`

Key settings:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/skyjet_db
spring.datasource.username=skyjet_user
spring.datasource.password=skyjet_password
app.jwt.secret=your-very-secure-secret-key...
app.jwt.expiration=86400000
```

### Change JWT Secret
In production, update the JWT secret in `application.properties`:
```properties
app.jwt.secret=your-production-secret-key-minimum-32-characters
```

## Project Structure
```
backend/
├── pom.xml                              Spring Boot dependencies
├── src/main/java/com/skyjet/backend/
│   ├── SkyJetBackendApplication.java   Main entry point
│   ├── entity/                         JPA entities (User, Flight, etc.)
│   ├── repository/                     Data access layer
│   ├── service/                        Business logic (Phase 2)
│   ├── controller/                     REST endpoints (Phase 2)
│   └── config/                         Security & CORS config (Phase 2)
├── src/main/resources/
│   ├── application.properties           Configuration
│   └── db/migration/                   Flyway migrations
└── src/test/                           Unit & integration tests
```

## Troubleshooting

### PostgreSQL Connection Failed
```bash
# Check if container is running
docker ps | grep skyjet-postgres

# View logs
docker logs skyjet-postgres

# Restart containers
docker-compose restart
```

### Migrations Failed
```bash
# Clear Flyway history and restart
docker exec skyjet-postgres psql -U skyjet_user -d skyjet_db \
  -c "DROP TABLE IF EXISTS flyway_schema_history;"
docker-compose restart
mvn clean spring-boot:run
```

### Port Already in Use
If port 5432 is already in use, modify `docker-compose.yml`:
```yaml
postgres:
  ports:
    - "5433:5432"  # Change to 5433
```

Then update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/skyjet_db
```

## Next Steps (Phase 2)
- Implement AuthService & AuthController
- Add JWT token generation and validation
- Create Flight, Booking, User services
- Implement REST endpoints for all CRUD operations
- Add input validation and error handling

## API Documentation
Coming in Phase 2: Full REST API documentation with request/response examples
