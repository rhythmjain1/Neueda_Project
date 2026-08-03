# Transaction Monitoring and Alerting System (TMS)

Enterprise-grade financial transaction monitoring platform built with Java 17 + Spring Boot 3, React + Tailwind CSS, and MySQL 8 — fully containerized with Docker.

---

## 🚀 Quick Start

```bash
# 1. Copy environment file
cp .env.example .env

# 2. Start all services
docker compose up --build -d

# 3. Access the dashboard
open http://localhost:3000

# Default credentials
# Username: admin
# Password: admin123
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Docker Compose                       │
│  ┌─────────────┐   ┌──────────────┐   ┌─────────────┐  │
│  │  Frontend   │   │   Backend    │   │    MySQL    │  │
│  │ React+Vite  │◄──│ Spring Boot  │◄──│  Database   │  │
│  │  Port 3000  │   │  Port 8080   │   │  Port 3306  │  │
│  └─────────────┘   └──────────────┘   └─────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 📋 Tech Stack

| Layer       | Technology             |
|-------------|------------------------|
| Backend     | Java 17 + Spring Boot 3.2 |
| Security    | Spring Security + JWT  |
| Database    | MySQL 8.0 + JPA/Hibernate |
| Frontend    | React 18 + Vite + Tailwind CSS |
| Container   | Docker + Docker Compose |
| CI/CD       | Jenkins (Jenkinsfile included) |

---

## 📁 Project Structure

```
Neueda_Project/
├── backend/                # Spring Boot application
│   ├── src/main/java/com/neueda/tms/
│   │   ├── config/         # Security, DataInitializer
│   │   ├── controller/     # REST controllers
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Spring Data JPA repos
│   │   ├── model/          # JPA entities
│   │   ├── dto/            # Request/Response DTOs
│   │   ├── rules/          # Monitoring rule evaluators
│   │   ├── security/       # JWT provider + filter
│   │   └── exception/      # Global error handler
│   └── Dockerfile
├── frontend/               # React dashboard
│   ├── src/
│   │   ├── api/            # Axios modules
│   │   ├── components/     # Reusable components
│   │   ├── pages/          # Route-level pages
│   │   ├── context/        # AuthContext
│   │   └── hooks/          # Custom hooks
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yml
├── Jenkinsfile
└── .env.example
```

---

## 🔍 Monitoring Rules

| Rule Code | Description | Default |
|-----------|-------------|---------|
| `HIGH_AMOUNT` | Amount exceeds threshold | > 10,000 |
| `RAPID_TRANSACTIONS` | Many tx from same account | > 5 in 10 min |
| `RESTRICTED_COUNTRY` | Tx from restricted country | North Korea (KP) |
| `NEW_CUSTOMER_HIGH_AMOUNT` | New customer, high amount | > 5,000 |
| `ROUND_AMOUNT` | Round number (structuring) | Divisible by 1000 |
| `ODD_HOURS` | Transaction at odd hours | 00:00–04:00 |

All thresholds are configurable from the **Rules** dashboard.

---

## 🌐 API Endpoints

### Public
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Get JWT token |
| POST | `/api/transactions` | Submit transaction |

### Protected (JWT Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/transactions` | List/search transactions |
| GET | `/api/alerts` | List/search alerts |
| POST | `/api/alerts/{id}/forward` | Forward to investigation |
| POST | `/api/alerts/{id}/dismiss` | Dismiss alert |
| POST | `/api/alerts/{id}/close` | Close alert |
| GET | `/api/alerts/investigation` | Investigation queue |
| GET | `/api/dashboard/stats` | Live dashboard stats |
| GET | `/api/rules` | List rules |
| PUT | `/api/rules/{id}` | Update rule config |
| GET | `/api/reports/transactions` | Transaction report |
| GET | `/api/reports/alerts` | Alert history report |
| GET | `/api/reports/audit` | Audit trail report |

---

## 🧪 Testing

```bash
# Backend unit tests
cd backend && mvn test

# Frontend build check
cd frontend && npm ci && npm run build
```

---

## 🔐 Security Features

- JWT authentication (15-min access tokens)
- BCrypt password hashing (strength 12)
- CORS restricted to frontend origin
- Security headers (XSS, CSRF, CSP) via Nginx
- SQL injection prevention via JPA parameterized queries
- Non-root Docker container users
- All secrets via environment variables

---

## 🔧 Local Development

```bash
# Backend (requires MySQL running)
cd backend && mvn spring-boot:run

# Frontend
cd frontend && npm install && npm run dev
# → http://localhost:5173 (proxies /api to localhost:8080)
```
