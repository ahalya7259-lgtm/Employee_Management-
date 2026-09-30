# EmployeeHub — Enterprise Employee Management System

A modern, secure, full-stack employee management platform built with **Java 21**, **Spring Boot 3**, **React**, **TypeScript**, and **MySQL**.

> Portfolio-grade project demonstrating professional Java Full Stack development practices.

## Features

- **Authentication & Authorization** — JWT-based auth with role-based access (ADMIN, HR, EMPLOYEE)
- **Employee Management** — CRUD, search, filter, soft-delete, unique employee codes
- **Department Management** — Create, update, assign heads, employee counts
- **Attendance** — Check-in / check-out, status (Present / Late / Half-day), history
- **Leave Management** — Request, balance tracking, approve/reject with audit history
- **Dashboard & Analytics** — Real-time stats from the database
- **API Documentation** — OpenAPI / Swagger UI
- **Docker** — One-command local stack with Docker Compose

## Tech Stack

| Layer        | Technology                          |
|-------------|--------------------------------------|
| Backend     | Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA, Flyway |
| Auth        | JWT (jjwt), BCrypt                   |
| Database    | MySQL 8                              |
| Frontend    | React, TypeScript, Vite, Tailwind CSS |
| API Docs    | springdoc-openapi                    |
| DevOps      | Docker, Docker Compose, Maven        |

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 20+
- MySQL 8 (or use Docker)
- Docker & Docker Compose (optional)

### 1. Clone

```bash
git clone https://github.com/ahalya7259-lgtm/Employee_Management-.git
cd Employee_Management-
```

### 2. Environment

```bash
cp .env.example .env
# Edit .env if needed
```

### 3. Start with Docker Compose (recommended)

```bash
docker compose up -d --build
```

Backend: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html  
API Docs: http://localhost:8080/api-docs

### 4. Run Backend locally (without Docker)

```bash
# Start MySQL and create database `employeehub`
cd backend
mvn spring-boot:run
```

### 5. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend: http://localhost:5173

## Demo Credentials

| Role     | Email                      | Password   |
|----------|----------------------------|------------|
| ADMIN    | admin@employeehub.com      | Admin@123  |
| HR       | hr@employeehub.com         | Hr@12345   |
| EMPLOYEE | john.doe@employeehub.com   | Emp@12345  |
| EMPLOYEE | jane.smith@employeehub.com | Emp@12345  |

## API Overview

Base path: `/api/v1`

| Module       | Endpoints                                      |
|--------------|------------------------------------------------|
| Auth         | `POST /auth/login`                             |
| Employees    | `GET/POST /employees`, `GET/PUT/DELETE /employees/{id}` |
| Departments  | `GET/POST /departments`, `GET/PUT /departments/{id}` |
| Attendance   | `POST /attendance/check-in`, `check-out`, `GET /attendance` |
| Leaves       | `POST /leaves`, `POST /leaves/{id}/approve`, `reject` |
| Dashboard    | `GET /dashboard/stats`                         |

Full interactive docs available at `/swagger-ui.html` after starting the backend.

## Project Structure

```
employeehub/
├── backend/                 # Spring Boot application
│   ├── src/main/java/com/employeehub/
│   │   ├── auth/            # Authentication
│   │   ├── employee/        # Employee module
│   │   ├── department/      # Department module
│   │   ├── attendance/      # Attendance module
│   │   ├── leave/           # Leave module
│   │   ├── dashboard/       # Analytics
│   │   ├── security/        # JWT & security
│   │   ├── config/          # Configuration
│   │   └── exception/       # Global exception handling
│   └── src/main/resources/db/migration/  # Flyway migrations
├── frontend/                # React + TypeScript + Vite
├── docs/                    # Architecture & interview guides
├── docker-compose.yml
└── README.md
```

## Architecture

- **Controller → Service → Repository → Database**
- DTOs for all API boundaries (no entity leakage)
- Constructor injection
- Centralized exception handling
- Role-based method security (`@PreAuthorize`)
- Soft deletion for employees
- Flyway for schema versioning (Hibernate `ddl-auto: validate`)

## Security Notes

- Passwords hashed with BCrypt
- JWT secrets via environment variables
- CORS restricted by configuration
- Sensitive fields (salary) filtered by role
- No secrets committed to the repository

## Documentation

| Document | Description |
|----------|-------------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design |
| [docs/DATABASE.md](docs/DATABASE.md) | Schema & ER diagram |
| [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md) | Endpoint reference |
| [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) | Detailed setup |
| [docs/SECURITY.md](docs/SECURITY.md) | Security model |
| [docs/INTERVIEW_GUIDE.md](docs/INTERVIEW_GUIDE.md) | Interview preparation |

## Known Limitations

- Frontend is a functional starter; full UI polish and all pages are in progress
- Report CSV export and advanced analytics charts planned
- Testcontainers integration tests can be expanded
- Production hardening (rate limiting, refresh tokens) left as future work

## License

MIT
