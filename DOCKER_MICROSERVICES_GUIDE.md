# EventEase Microservices & Docker Architecture Guide

This guide documents the **Microservices Architecture** and **Docker Deployment format** implemented for the EventEase platform.

---

## 1. Microservices Architecture Overview

The system is decomposed into decoupled, single-responsibility microservices fronted by an **API Gateway** and backed by a containerized **MySQL 8.0** database.

```mermaid
flowchart TD
    subgraph ClientLayer ["Client Layer"]
        Browser["User Web Browser"]
    end

    subgraph PresentationLayer ["Frontend Container (:3000)"]
        ReactUI["React / Vite SPA (Nginx)"]
    end

    subgraph GatewayLayer ["API Gateway (:8080)"]
        APIGateway["Nginx Reverse Proxy & Route Dispatcher"]
    end

    subgraph MicroservicesLayer ["Microservices Layer"]
        UserService["User & Auth Service (:8081)"]
        EventService["Event & Category Service (:8082)"]
        RegService["Registration & Bookmark Service (:8083)"]
        NotifService["Notification Service (:8084)"]
    end

    subgraph DataLayer ["Data Layer (:3306)"]
        MySQL[("MySQL 8.0 Database\n(event_management)")]
    end

    Browser -->|HTTP :3000| ReactUI
    ReactUI -->|REST API :8080| APIGateway

    APIGateway -->|/api/auth/*, /api/users/me| UserService
    APIGateway -->|/api/events/*, /api/categories/*| EventService
    APIGateway -->|/api/events/{id}/register*, bookmarks| RegService
    APIGateway -->|/api/notifications/*| NotifService

    UserService -->|JDBC| MySQL
    EventService -->|JDBC| MySQL
    RegService -->|JDBC| MySQL
    NotifService -->|JDBC| MySQL
```

---

## 2. Microservice Roster & Responsibilities

| Container / Service | Port | Technology | Domain Responsibilities |
| :--- | :--- | :--- | :--- |
| **`frontend`** | `3000` | React 19, Vite, Nginx Alpine | Responsive Single Page Application, UI dashboard, event catalog, booking modals |
| **`api-gateway`** | `8080` | Nginx 1.27 Alpine | Unified API entry point, CORS headers, path-based routing, proxy buffering, `/health` endpoint |
| **`user-service`** | `8081` | Spring Boot, Spring Security, JDBC | User sign-up, sign-in, BCrypt passwords, stateless JWT token issuing, profile retrieval |
| **`event-service`** | `8082` | Spring Boot, Spring Security, JDBC | Event creation, editing, category catalog, event search & filtering, capacity limits |
| **`registration-service`** | `8083` | Spring Boot, Spring Security, JDBC | Thread-safe event registrations, attendee limits, duplicate booking prevention, bookmarks |
| **`notification-service`** | `8084` | Spring Boot, Spring Security, JDBC | In-app notification creation, read status tracking, unread badges |
| **`mysql-db`** | `3306` | MySQL 8.0 Official | Relational persistence, transactional integrity, auto-seeded on container initial start |

---

## 3. Directory Layout

```
.
├── docker-compose.yml              # Primary Microservices orchestration file
├── docker-compose.monolith.yml     # Alternative Monolithic orchestration file
├── .env                            # Active environment configuration
├── .env.example                    # Environment configuration template
├── .dockerignore                   # Global Docker build ignore
├── DOCKER_MICROSERVICES_GUIDE.md   # Complete microservices documentation
├── database/
│   ├── init.sql                    # Auto-entrypoint script mounted to MySQL container
│   ├── schema.sql                  # Table definitions, constraints, indexes
│   ├── seed.sql                    # Pre-seeded users, categories, events
│   └── setup.sql                   # Standalone DB setup script
├── api-gateway/
│   ├── Dockerfile                  # Lightweight Nginx gateway container
│   └── nginx.conf                  # Route dispatch rules & CORS configuration
├── eventease-frontend/
│   ├── Dockerfile                  # Multi-stage Node builder + Nginx runner
│   ├── nginx.conf                  # SPA fallback & asset cache configuration
│   └── src/...                     # React frontend source
└── services/
    ├── user-service/               # User & Auth microservice (:8081)
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/...
    ├── event-service/              # Events & Categories microservice (:8082)
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/...
    ├── registration-service/       # Registrations & Bookmarks microservice (:8083)
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/...
    └── notification-service/       # Notifications microservice (:8084)
        ├── Dockerfile
        ├── pom.xml
        └── src/...
```

---

## 4. Docker Commands Reference

### 4.1 Launching the Full Microservices Stack
```bash
# 1. Ensure .env is present
cp .env.example .env

# 2. Build and launch all 7 containers in detached mode
docker compose up -d --build

# 3. Check health and running status of all containers
docker compose ps
```

### 4.2 Inspecting Logs
```bash
# Tail logs across all services
docker compose logs -f

# Tail logs of a specific microservice
docker compose logs -f user-service
docker compose logs -f event-service
docker compose logs -f registration-service
docker compose logs -f api-gateway
docker compose logs -f mysql-db
```

### 4.3 Verifying Health
```bash
# Verify API Gateway Health
curl -s http://localhost:8080/health

# Verify Public Events through Gateway
curl -s http://localhost:8080/api/events

# Verify Categories through Gateway
curl -s http://localhost:8080/api/categories
```

### 4.4 Stopping & Cleaning Up
```bash
# Stop containers without losing database volume
docker compose down

# Stop containers and remove volumes (clean slate)
docker compose down -v
```

### 4.5 Alternative: Running the Monolithic Stack
If you prefer running a single Spring Boot container alongside the frontend and database:
```bash
docker compose -f docker-compose.monolith.yml up -d --build
```

---

## 5. Security & Stateless Inter-Service Communication

1. **Authentication Flow**:
   - The user submits login credentials (`POST /api/auth/login`).
   - The **API Gateway** routes this to `user-service:8081`.
   - `user-service` validates BCrypt password hashes and generates a signed **HMAC-SHA256 JWT**.
   - The token encapsulates user claims: `sub` (email), `userId`, `role` (`USER` or `ADMIN`), and `name`.

2. **Stateless Verification in Microservices**:
   - Every microservice shares the configured `APP_JWT_SECRET` key via environment variables.
   - When requests containing `Authorization: Bearer <token>` pass through the API Gateway, downstream microservices independently validate signature and expiration without making synchronous roundtrips to `user-service`.

3. **CORS & Gateway Route Translation**:
   - The frontend communicates solely with `http://localhost:8080/api`.
   - Nginx handles preflight `OPTIONS` requests and sets `Access-Control-Allow-Origin: *`.
   - Requests are routed based on prefix and regex patterns to ensure existing frontend API clients operate with zero modification.

---

## 6. Access Endpoints

- **Frontend User Interface**: [http://localhost:3000](http://localhost:3000)
- **API Gateway**: [http://localhost:8080](http://localhost:8080)
- **User Service (Direct)**: `http://localhost:8081`
- **Event Service (Direct)**: `http://localhost:8082`
- **Registration Service (Direct)**: `http://localhost:8083`
- **Notification Service (Direct)**: `http://localhost:8084`
- **MySQL Database**: `localhost:3306` (User: `eventease`, DB: `event_management`)
