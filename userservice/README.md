# Event Booking & Ticketing System 🎟️

A production-grade microservices-based event booking platform built with Spring Boot, Java 21, and modern cloud-native technologies.

## 🏗️ Architecture

**Microservices Architecture** with database-per-service pattern, event-driven communication (Kafka), and API Gateway.

### Services Overview

| Service | Port | Status | Description |
|---------|------|--------|-------------|
| User Service | 8084 | ✅ Complete | Authentication & User Management |
| Event Service | 8081 | 🚧 In Progress | Event CRUD & Management |
| Booking Service | 8083 | 📋 Planned | Booking & Ticket Management |
| Payment Service | 8085 | 📋 Planned | Payment Processing (Paystack) |
| Notification Service | 8086 | 📋 Planned | Email/SMS Notifications |
| Analytics Service | 8087 | 📋 Planned | Reporting & Analytics |
| API Gateway | 8080 | 📋 Planned | Routing & Load Balancing |

## 🚀 Tech Stack

### Backend
- **Language:** Java 21
- **Framework:** Spring Boot 3.2.0
- **Security:** Spring Security + JWT
- **Database:** PostgreSQL (database-per-service)
- **Messaging:** Apache Kafka
- **API Gateway:** Spring Cloud Gateway
- **Service Discovery:** Netflix Eureka
- **Documentation:** Swagger/OpenAPI

### DevOps
- **Containerization:** Docker
- **Orchestration:** Kubernetes
- **CI/CD:** GitHub Actions
- **Monitoring:** Prometheus + Grafana

## 📋 Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- PostgreSQL 15+
- Docker (optional)

### Running Services Locally

#### User Service
```bash
cd userservice
mvn spring-boot:run
```
**Swagger:** http://localhost:8084/swagger-ui.html

#### Event Service (Coming Soon)
```bash
cd eventservice
mvn spring-boot:run
```

## 📚 API Documentation

### User Service ✅
- `POST /api/auth/register` - Register new user (201 Created)
- `POST /api/auth/login` - Login with username/email (200 OK)

### Event Service 🚧
- `POST /api/events` - Create event
- `GET /api/events` - List events
- `GET /api/events/{id}` - Get event details
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Delete event

## 🗂️ Project Structure
```
event-booking-system/
├── userservice/           # Authentication service
├── eventservice/          # Event management service
├── bookingservice/        # Booking service
├── paymentservice/        # Payment service
├── notificationservice/   # Notification service
├── analyticsservice/      # Analytics service
├── apigateway/           # API Gateway
└── docker-compose.yml    # Multi-service orchestration
```

## ✅ Development Roadmap

**Phase 1: Core Services (MVP)**
- [x] User Service - Authentication with JWT
- [ ] Event Service - Event CRUD operations
- [ ] Booking Service - Booking flow

**Phase 2: Infrastructure**
- [ ] Apache Kafka - Event-driven communication
- [ ] API Gateway - Routing
- [ ] Service Discovery - Eureka

**Phase 3: Additional Services**
- [ ] Payment Service - Paystack integration
- [ ] Notification Service - Email/SMS
- [ ] Analytics Service - Reporting

**Phase 4: Deployment**
- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline

## 🤝 Contributing

This is a learning project. Feel free to fork and experiment!

## 📄 License

MIT

## 👨‍💻 Author

**Senibo Don-Pedro**
- Email: senibodonpedro@gmail.com
- GitHub: [@your-username](https://github.com/your-username)