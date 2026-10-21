# Architecture

## Overview

EmployeeHub follows a modular, layered architecture:

Client (React) to REST API (Spring Boot) to Service Layer to Repository (JPA) to MySQL.

## Backend Modules

- auth: Login, JWT issuance
- employee: Employee lifecycle
- department: Department CRUD
- attendance: Check-in/out, history
- leave: Requests, balances, approvals
- dashboard: Aggregated statistics
- security: JWT filter, UserDetails
- config: Security, OpenAPI, DataInitializer
- exception: Global exception handling

## Design Decisions

- DTOs only on API boundary. Entities never leave the service layer.
- Constructor injection for all dependencies.
- Soft delete: Employees are deactivated (deleted_at) rather than hard-deleted.
- Flyway migrations. Schema is versioned; Hibernate runs in validate mode.
- UTC timestamps. All Instant values stored in UTC; attendance date is the UTC calendar date of check-in.
- Role-based method security with PreAuthorize on controllers; frontend role checks are for UX only.

## Request Flow

1. Request hits JWT filter, validates token, sets SecurityContext.
2. Controller validates input (Bean Validation).
3. Service executes business rules inside a transaction.
4. Repository performs persistence.
5. Response mapped to DTO and wrapped in ApiResponse.
