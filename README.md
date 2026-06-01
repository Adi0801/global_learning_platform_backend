# Global Learning Platform Backend

## Project Overview

This is a Spring Boot backend for a global live-learning platform. Teachers create course offerings with multiple sessions, and parents book the complete offering. The system prevents duplicate bookings, detects overlapping session times, handles concurrent booking attempts, and converts session times across teacher and parent timezones.

## Tech Stack Used

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- H2 in-memory database for tests
- Swagger/OpenAPI
- Lombok

## Setup Instructions

Create a PostgreSQL database:

```sql
CREATE DATABASE global_learning_platform;
```

Create a `.env` file in the project root. A sample is available in `.env.example`.

Run Flyway migrations and start the app through Spring Boot:

```bash
.\mvnw.cmd spring-boot:run
```

Run tests:

```bash
.\mvnw.cmd test
```

## Environment Variables Required

```env
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/global_learning_platform
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

These values are consumed in `src/main/resources/application.properties`:

```properties
server.port=${SERVER_PORT}
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

## API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

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

Book offering request:

```json
{
  "parentId": "dddddddd-dddd-dddd-dddd-dddddddddddd",
  "offeringId": "10000000-0000-0000-0000-000000000003"
}
```

Conflict response example:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Offering conflicts with one or more already booked sessions"
}
```

## Database Schema Overview

Flyway migration file:

```text
src/main/resources/db/migration/V1__create_global_learning_platform_schema.sql
```

Main tables:

- `courses`
- `teachers`
- `parents`
- `offerings`
- `sessions`
- `bookings`

Important constraints:

- `sessions.end_at_utc > sessions.start_at_utc`
- One confirmed booking per parent and offering
- Offering status: `DRAFT`, `PUBLISHED`, `CANCELLED`
- Booking status: `CONFIRMED`, `CANCELLED`

Duplicate confirmed booking is blocked by:

```sql
CREATE UNIQUE INDEX ux_confirmed_booking_parent_offering
ON bookings(parent_id, offering_id)
WHERE status = 'CONFIRMED';
```

## Assumptions Made

- Parents book the full offering, not individual sessions.
- Every offering can have one or more sessions.
- Session times are stored in UTC.
- API clients pass valid IANA timezone names such as `Asia/Kolkata`, `Europe/London`, or `America/New_York`.
- Authentication and authorization are out of scope for this assignment.
- Capacity management is not required because the assignment focuses on parent schedule conflicts.

## Concurrency Handling Approach

Booking is handled inside a database transaction.

Before conflict checking, the parent row is locked using pessimistic write locking:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<ParentProfile> findByIdForUpdate(UUID id);
```

This ensures simultaneous booking requests for the same parent are processed one at a time. After the first request commits, the second request sees the new booking and fails if it is duplicate or overlapping.

## Timezone Handling Approach

Teachers create sessions in their own timezone. The backend converts those local times to UTC before saving.

Parents request offerings or bookings in their timezone:

```http
GET /api/v1/parent/offerings?timeZone=Europe/London
```

The response converts stored UTC session times into the requested timezone. This keeps conflict detection consistent while still showing local times correctly to parents.

## Booking Scenarios Covered

### Successful Booking

If the parent has no duplicate or overlapping booking, the API returns `201 Created` with booking status `CONFIRMED`.

<img width="678" height="722" alt="Successful booking" src="https://github.com/user-attachments/assets/4ace9113-cc07-4d10-9369-ef755cfcabdf" />

### Duplicate Booking

If the same parent books the same offering again, the API returns `409 Conflict`.

<img width="698" height="702" alt="Duplicate booking conflict" src="https://github.com/user-attachments/assets/bbce033c-3080-4a97-9c82-f35236bb7831" />

### Overlapping Session Conflict

Example:

```text
Python Offering : 2026-06-13 18:00 - 19:00 IST
Roblox Offering : 2026-06-13 18:30 - 19:30 IST
Overlap         : 18:30 - 19:00
```

The overlap rule is:

```text
existing_start < candidate_end
AND
existing_end > candidate_start
```

If any session overlaps, booking fails with `409 Conflict`.

### Timezone Conversion

The same stored UTC session is shown in the parent's requested timezone.

<img width="988" height="812" alt="Timezone conversion response" src="https://github.com/user-attachments/assets/d002f8ca-4d96-4581-99b6-8495f5c0ccd6" />

<img width="1317" height="786" alt="Timezone conversion database view" src="https://github.com/user-attachments/assets/efb61b36-31f5-4fbf-bacb-6463e54b4d4c" />

## Steps to Run the Application Locally

1. Create PostgreSQL database:

```sql
CREATE DATABASE global_learning_platform;
```

2. Create `.env` file:

```env
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/global_learning_platform
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

3. Start the application:

```bash
.\mvnw.cmd spring-boot:run
```

4. Open Swagger:

```text
http://localhost:8080/swagger-ui.html
```

5. Run tests:

```bash
.\mvnw.cmd test
```
