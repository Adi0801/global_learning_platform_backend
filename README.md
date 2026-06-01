# Global Learning Platform Backend

Spring Boot backend for a live-class booking system. Teachers create offerings with multiple sessions. Parents book a complete offering, and the backend prevents duplicate or overlapping bookings across timezones.

## Tech Stack

- Java 17, Spring Boot 4
- Spring Web MVC, Spring Data JPA
- PostgreSQL, Flyway
- H2 for tests
- Swagger/OpenAPI

## Project Structure

```text
config       Swagger config
controller   REST APIs
dto          Request/response DTOs
entity       JPA entities
exception    Global exception handler
repository   JPA repositories
service      Business logic
```

## Configuration

Runtime values are loaded from `.env`:

```env
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/global_learning_platform
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

`application.properties` uses these variables:

```properties
server.port=${SERVER_PORT}
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

`.env.example` is included for reference.

## APIs

Teacher APIs:

```http
POST /api/v1/teacher/offerings
POST /api/v1/teacher/offerings/{offeringId}/sessions
GET  /api/v1/teacher/{teacherId}/offerings
```

Parent APIs:

```http
GET  /api/v1/parent/offerings?timeZone=Europe/London
POST /api/v1/parent/bookings
GET  /api/v1/parent/{parentId}/bookings?timeZone=Europe/London
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

## Scenario Handling

### 1. Successful Offering Booking

A parent books the complete offering, not a single session.

```json
{
  "parentId": "dddddddd-dddd-dddd-dddd-dddddddddddd",
  "offeringId": "10000000-0000-0000-0000-000000000003"
}
```

The backend creates one `Booking` record for the parent and offering. All sessions under that offering are considered booked for that parent.

### 2. Duplicate Booking

If the same parent tries to book the same offering again, the booking fails with `409 Conflict`.

Handled by:

- Service-level duplicate check
- PostgreSQL partial unique index:

```sql
CREATE UNIQUE INDEX ux_confirmed_booking_parent_offering
ON bookings(parent_id, offering_id)
WHERE status = 'CONFIRMED';
```

Response:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Parent has already booked this offering"
}
```

### 3. Overlapping Session Conflict

Even though booking is at offering level, conflict checking is done at session level.

Example:

```text
Python Offering : 2026-06-13 18:00 - 19:00 IST
Roblox Offering : 2026-06-13 18:30 - 19:30 IST

Overlap         : 18:30 - 19:00
```

This must fail with `409 Conflict`.

The overlap check uses:

```text
existing_start < candidate_end
AND
existing_end > candidate_start
```

If any session in the new offering overlaps with any already booked session for that parent, booking is rejected.

Response:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Offering conflicts with one or more already booked sessions"
}
```

### 4. Concurrent Booking Requests

The booking method runs inside a transaction and locks the parent row:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<ParentProfile> findByIdForUpdate(UUID id);
```

This means simultaneous booking requests for the same parent are processed one by one. The second request sees the first committed booking and fails if it is duplicate or overlapping.

### 5. Timezone Conversion

Teachers create sessions in their own timezone. The backend converts them to UTC before saving.

Parents pass their timezone while viewing offerings or bookings:

```http
GET /api/v1/parent/offerings?timeZone=Europe/London
```

The response converts UTC session times into the parent's requested timezone.

IANA timezone names are used:

```text
Asia/Kolkata
Europe/London
America/New_York
```

## Database

Flyway migration:

```text
src/main/resources/db/migration/V1__create_global_learning_platform_schema.sql
```

Main tables:

```text
courses, teachers, parents, offerings, sessions, bookings
```

## Screenshots

**1. Successful booking**


<img width="678" height="722" alt="image" src="https://github.com/user-attachments/assets/4ace9113-cc07-4d10-9369-ef755cfcabdf" />


**2. Duplicate booking conflict**


<img width="698" height="702" alt="image" src="https://github.com/user-attachments/assets/bbce033c-3080-4a97-9c82-f35236bb7831" />


**3. Timezone conversion**


<img width="988" height="812" alt="image" src="https://github.com/user-attachments/assets/d002f8ca-4d96-4581-99b6-8495f5c0ccd6" />
<img width="1317" height="786" alt="image" src="https://github.com/user-attachments/assets/efb61b36-31f5-4fbf-bacb-6463e54b4d4c" />


## Run

Create PostgreSQL database:

```sql
CREATE DATABASE global_learning_platform;
```

Start:

```bash
.\mvnw.cmd spring-boot:run
```

Test:

```bash
.\mvnw.cmd test
```
