# Event Management System (EMS)

A full-featured web application for managing events, built with Java and Spring Boot. Developed as the final project for the JavaOOP course.

## Features

- **Role-based access**: three user roles — Administrator, Organizer, Attendee — with distinct workflows
- **Event lifecycle**: organizers create events in DRAFT, publish them, optionally cancel
- **Public catalog**: browse and search published events without an account
- **Ticket registration**: attendees register for events, get a unique ticket code, can cancel before start
- **Overbooking protection**: optimistic locking with automatic retry prevents race conditions on the last seat
- **File uploads**: organizers can attach a cover image to their events
- **Admin panel**: user management (activate/deactivate), category management, system-wide statistics
- **Custom error pages** for 400/403/404/500

## Tech stack

- **Java 17**
- **Spring Boot 4.0.6** — auto-configuration, embedded Tomcat
- **Spring MVC + Thymeleaf** — server-side rendering
- **Spring Security 6** — authentication, BCrypt password hashing, CSRF, role-based access
- **Spring Data JPA + Hibernate** — ORM, automatic dirty checking
- **Spring Retry** — automatic retry on optimistic lock conflicts
- **MySQL 8** — relational database
- **MapStruct** — compile-time Entity ↔ DTO mapping
- **Lombok** — boilerplate reduction (on DTOs)
- **Bootstrap 5** — responsive UI
- **JUnit 5 + Mockito + AssertJ** — testing
- **Maven** — build

## Object-oriented design

The project deliberately demonstrates the four pillars of OOP:

- **Encapsulation** — entity fields are private; state changes go through methods (`event.publish()`, `ticket.cancel()`) that enforce business invariants. No public setters for status fields.
- **Inheritance** — `BaseEntity` (abstract) → `User` (abstract) → `Administrator` / `Organizer` / `Attendee`, mapped to a single database table using JPA Single Table Inheritance.
- **Polymorphism** — `User.getUserType()` is abstract, overridden by each concrete subclass. Spring Security uses this polymorphic call to derive role authorities without knowing the concrete type.
- **Abstraction** — controllers depend on service interfaces (`UserService`, `EventService`, `TicketService`), not implementations.

### Design patterns applied

- **Factory Method** — `UserFactory` chooses the concrete `User` subclass based on `UserType`
- **Strategy** — `NotificationService` interface (extensible to email/SMS without changing consumers)
- **Repository** — Spring Data JPA derives queries from method names
- **DTO + Mapper** — `EventMapper` (MapStruct) decouples entities from HTTP layer
- **Builder** — Lombok `@Builder` on DTOs
- **Dependency Injection** — constructor injection throughout

## Architecture

Classic layered architecture:

```
Controller (HTTP)  →  Service (business rules)  →  Repository (data access)  →  Entity (domain)
```

Business rules live in the **service layer and inside entities** (rich domain model), not in controllers. Controllers stay thin.

### Package structure

```
com.erbol.ems
├── common         (BaseEntity, exceptions, FileStorageService)
├── config         (SecurityConfig, WebConfig, DataSeeder)
├── auth           (CustomUserDetailsService, AuthController, PageController)
├── user           (User hierarchy, UserService, UserFactory)
├── category       (Category CRUD)
├── event          (Event lifecycle, OrganizerEventController, PublicEventController)
├── ticket         (Ticket registration with anti-overbooking)
└── admin          (Admin dashboard, user management)
```

## Running locally

### Prerequisites

- JDK 17+
- MySQL 8 running on `localhost:3306`

### Database setup

```sql
CREATE DATABASE IF NOT EXISTS ems_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ems_user'@'localhost' IDENTIFIED BY 'ems_pass_2026';
GRANT ALL PRIVILEGES ON ems_db.* TO 'ems_user'@'localhost';
FLUSH PRIVILEGES;
```

### Build and run

```bash
./mvnw spring-boot:run
```

Application starts on `http://localhost:8080`.

### Default administrator credentials

Created automatically on first startup:

- Email: `admin@ems.local`
- Password: `Admin123!`

Default categories are also seeded (Technology, Music, Business, Education, Sports, Arts).

## Running tests

```bash
./mvnw test
```

Unit tests cover:
- User registration flow (`UserServiceImplTest`) — happy path, duplicate email, admin protection
- Event domain rules (`EventDomainTest`) — status transitions, time/capacity invariants

## Security highlights

- BCrypt password hashing (Spring Security default)
- CSRF protection enabled on all state-changing endpoints
- Role-based access control at URL level (`SecurityFilterChain`) AND method level (`@PreAuthorize`)
- Path traversal protection in file upload service
- Content-type and extension whitelist for image uploads
- Authentication-based deactivation: `CustomUserDetailsService` refuses inactive users

## Concurrency safety

`TicketService.register()` is annotated with `@Retryable` for `ObjectOptimisticLockingFailureException`.
The Event entity carries a `@Version` column. When two attendees race for the last seat, the second
request fails on the optimistic lock check, Spring Retry transparently re-executes the method,
the capacity check is re-evaluated against the new state, and the second attendee gets a clean
"sold out" error instead of an overbooking.

## Author

Erbol — JavaOOP final project, 2026
