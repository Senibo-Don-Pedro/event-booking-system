
# Event Service

Event management microservice for the Event Booking & Ticketing System. Handles event creation, updates, search, and publishing with JWT-based authentication.

## 🏗️ Architecture

```
┌─────────────────┐
│  User Service   │ (Port 8084)
│  (Auth & Users) │
└────────┬────────┘
         │ JWT Token
         ↓
┌─────────────────┐
│  Event Service  │ (Port 8081)
│ (Event CRUD &   │
│   Publishing)   │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│   PostgreSQL    │
│ event_service_db│
└─────────────────┘
```

## 🚀 Features

### Core Features
- ✅ **Create Events** - Organizers can create events with details (title, description, venue, pricing, capacity)
- ✅ **Update Events** - Update event details (owner-only)
- ✅ **Delete Events** - Soft delete by changing status to CANCELLED (owner-only)
- ✅ **Publish Events** - Change event status (DRAFT → PUBLISHED → CANCELLED/COMPLETED)
- ✅ **Search Events** - Dynamic search with filters (category, city, status, date range)
- ✅ **Pagination** - All list endpoints support pagination
- ✅ **Public Browsing** - Anyone can view published events (no auth required)

### Security Features
- ✅ **JWT Authentication** - Validates tokens issued by User Service
- ✅ **Authorization** - Owner-only operations (update, delete, publish)
- ✅ **Proper HTTP Status Codes** - 401 (Unauthorized), 403 (Forbidden), 404 (Not Found)
- ✅ **Global Exception Handling** - Standardized error responses

### Business Logic
- ✅ **Event Validation** - Start date must be in future, end date after start date
- ✅ **Capacity Management** - Prevents reducing capacity below tickets sold
- ✅ **Status Transitions** - Validates status changes (can't unpublish, can't uncomplete)
- ✅ **Event Categories** - Conference, Concert, Sports, Workshop, Seminar, Exhibition, Festival, Other
- ✅ **Event Status** - DRAFT, PUBLISHED, CANCELLED, COMPLETED

## 🛠️ Tech Stack

- **Java** 21
- **Spring Boot** 3.2.0
- **Spring Security** - JWT authentication
- **Spring Data JPA** - Database access with Specifications
- **PostgreSQL** - Database
- **Lombok** - Reduce boilerplate
- **Jackson** - JSON serialization with JSR-310 (Java 8 Date/Time)
- **Swagger/OpenAPI** 2.8.13 - API documentation
- **Maven** - Build tool

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.8+
- PostgreSQL 14+
- User Service running on port 8084 (for JWT authentication)

## ⚙️ Setup Instructions

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE event_service_db;

-- Verify
\c event_service_db
```

### 2. Application Configuration

```bash
# Navigate to resources directory
cd eventservice/src/main/resources

# Copy example configuration
cp application.properties.example application.properties

# Edit application.properties with your credentials
```

**Required Configuration:**

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/event_service_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# JWT (MUST match User Service secret!)
application.security.jwt.secret-key=YOUR_SECRET_KEY_FROM_USER_SERVICE
application.security.jwt.expiration=86400000
```

⚠️ **CRITICAL:** The JWT secret key MUST be identical to User Service, otherwise token validation will fail!

### 3. Build & Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

**Service will start on:** http://localhost:8081

**Swagger UI:** http://localhost:8081/swagger-ui.html

## 📡 API Endpoints

### Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/events/{id}` | Get event by ID |
| GET | `/api/events/published` | Get all published events (paginated) |
| GET | `/api/events/search` | Search events with filters |

### Protected Endpoints (JWT Required)

| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/api/events` | Create new event | Any authenticated user |
| PUT | `/api/events/{id}` | Update event | Event owner only |
| DELETE | `/api/events/{id}` | Delete (cancel) event | Event owner only |
| GET | `/api/events/my-events` | Get my events | Authenticated user |
| PATCH | `/api/events/{id}/status` | Update event status | Event owner only |

## 🧪 Testing

### Using curl

**1. Get JWT Token (from User Service)**
```bash
TOKEN=$(curl -s -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "your_username",
    "password": "your_password"
  }' | jq -r '.data.token')
```

**2. Create Event**
```bash
curl -X POST http://localhost:8081/api/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Tech Conference 2025",
    "description": "A conference about the future of technology",
    "category": "CONFERENCE",
    "imageUrl": "https://example.com/banner.jpg",
    "startDateTime": "2025-04-20T10:00:00",
    "endDateTime": "2025-04-20T16:00:00",
    "venue": "Eko Convention Center",
    "address": "Plot 1415 Adetokunbo Ademola Street",
    "city": "Lagos",
    "capacity": 300,
    "price": 15000.00
  }'
```

**3. Search Published Events**
```bash
curl "http://localhost:8081/api/events/published?page=0&size=20"
```

**4. Search by Category and City**
```bash
curl "http://localhost:8081/api/events/search?category=CONFERENCE&city=Lagos&page=0&size=20"
```

**5. Get My Events**
```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/api/events/my-events?page=0&size=20"
```

**6. Update Event Status (Publish)**
```bash
curl -X PATCH "http://localhost:8081/api/events/{eventId}/status?status=PUBLISHED" \
  -H "Authorization: Bearer $TOKEN"
```

### Using Swagger UI

1. Open http://localhost:8081/swagger-ui.html
2. Click the lock icon 🔒 (top right)
3. Enter: `Bearer YOUR_JWT_TOKEN`
4. Click "Authorize"
5. Test endpoints interactively

## 🗂️ Project Structure

```
eventservice/
├── src/main/java/com/senibo/eventservice/
│   ├── config/
│   │   ├── JacksonConfig.java          # Jackson date/time configuration
│   │   ├── SecurityConfig.java         # Spring Security + JWT
│   │   └── SwaggerConfig.java          # OpenAPI documentation
│   ├── controller/
│   │   └── EventController.java        # REST API endpoints
│   ├── dto/
│   │   ├── CreateEventRequest.java     # Create event DTO
│   │   ├── UpdateEventRequest.java     # Update event DTO
│   │   ├── EventResponse.java          # Event response DTO
│   │   ├── EventSearchRequest.java     # Search filters DTO
│   │   ├── PagedResponse.java          # Pagination wrapper
│   │   ├── ApiSuccessResponse.java     # Success response wrapper
│   │   └── ApiErrorResponse.java       # Error response wrapper
│   ├── entity/
│   │   └── Event.java                  # Event JPA entity
│   ├── enums/
│   │   ├── EventCategory.java          # Event categories enum
│   │   └── EventStatus.java            # Event status enum
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java # Global error handling
│   │   ├── NotFoundException.java      # 404 exception
│   │   ├── UnauthorizedException.java  # 403 exception
│   │   └── ValidationException.java    # 400 exception
│   ├── repository/
│   │   └── EventRepository.java        # JPA repository with Specifications
│   ├── security/
│   │   └── AuthTokenFilter.java        # JWT validation filter
│   ├── service/
│   │   ├── EventService.java           # Service interface
│   │   ├── JwtService.java             # JWT utilities
│   │   └── impl/
│   │       └── EventServiceImpl.java   # Business logic implementation
│   └── util/
│       └── EventSpecification.java     # Dynamic query specifications
└── src/main/resources/
    ├── application.properties          # Configuration (gitignored)
    └── application.properties.example  # Configuration template
```

## 📊 Database Schema

```sql
CREATE TABLE events (
    id                  UUID PRIMARY KEY,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    category            VARCHAR(50) NOT NULL,
    image_url           VARCHAR(500),
    start_date_time     TIMESTAMP NOT NULL,
    end_date_time       TIMESTAMP NOT NULL,
    venue               VARCHAR(200) NOT NULL,
    address             VARCHAR(500) NOT NULL,
    city                VARCHAR(100) NOT NULL,
    capacity            INTEGER NOT NULL,
    available_tickets   INTEGER NOT NULL,
    price               DECIMAL(10,2) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    organizer_id        UUID NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);
```

## 🔐 Security

### Authentication Flow

```
1. User logs in via User Service → Receives JWT token
2. User includes JWT in Authorization header: Bearer <token>
3. Event Service validates JWT (verifies signature with shared secret)
4. Event Service extracts userId from JWT subject
5. Event Service checks ownership for protected operations
```

### Authorization Rules

- **Create Event**: Any authenticated user
- **View Event**: Public (no auth) for published events
- **Update Event**: Event owner only
- **Delete Event**: Event owner only
- **Update Status**: Event owner only

## 🐛 Troubleshooting

### Issue: "Authentication required" even with token

**Solution:** Verify JWT secret matches User Service:
```bash
# Check User Service application.properties
cat userservice/src/main/resources/application.properties | grep jwt.secret

# Check Event Service application.properties  
cat eventservice/src/main/resources/application.properties | grep jwt.secret

# They MUST be identical!
```

### Issue: Date fields showing as null

**Solution:** Dates must be in ISO-8601 format: `yyyy-MM-dd'T'HH:mm:ss`
```json
{
  "startDateTime": "2025-04-20T10:00:00",
  "endDateTime": "2025-04-20T16:00:00"
}
```

### Issue: Invalid enum value error

**Valid Categories:** CONFERENCE, CONCERT, SPORTS, WORKSHOP, SEMINAR, EXHIBITION, FESTIVAL, OTHER

**Valid Status:** DRAFT, PUBLISHED, CANCELLED, COMPLETED

## 🎯 Next Steps

- [ ] Add Booking Service (book tickets for events)
- [ ] Add Payment Service (Paystack integration)
- [ ] Add Notification Service (email confirmations)
- [ ] Add API Gateway (single entry point)
- [ ] Add Service Discovery (Eureka)
- [ ] Containerize with Docker
- [ ] Deploy to cloud (Railway, Render, or Oracle Cloud)

## 👤 Author

**Senibo Don-Pedro**

## 📄 License

This project is part of a learning exercise for building microservices architecture.

---

**Part of the Event Booking & Ticketing System**
```

---

## **STEP 2: Create Root README**

### **Your Task:** Create `/README.md` (root level)

```markdown
# Event Booking & Ticketing System

A microservices-based event booking and ticketing platform built with Spring Boot and PostgreSQL. Users can browse events, create bookings, and manage tickets with secure JWT authentication.

## 🏗️ System Architecture

```
┌─────────────────┐
│  User Service   │ (Port 8084) - Authentication & User Management
└────────┬────────┘
         │ Issues JWT Tokens
         │
         ↓
┌─────────────────┐
│  Event Service  │ (Port 8081) - Event Management & Publishing
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│ Booking Service │ (Port 8083) - [Coming Soon]
└─────────────────┘

Each service has its own PostgreSQL database (Database-per-Service pattern)
```

## 📦 Services

### ✅ User Service (Complete)
- User registration & authentication
- JWT token generation
- Password encryption (BCrypt)
- Email verification (prepared for Notification Service)

**Port:** 8084  
**Database:** `user_service_db`  
[View Documentation](./userservice/README.md)

### ✅ Event Service (Complete)
- Event CRUD operations
- Dynamic search with filters
- Pagination support
- Public event browsing
- Owner-only event management

**Port:** 8081  
**Database:** `event_service_db`  
[View Documentation](./eventservice/README.md)

### 🚧 Booking Service (Planned)
- Create bookings
- Manage tickets
- Booking history
- Prevent overbooking

**Port:** 8083  
**Database:** `booking_service_db`

## 🛠️ Tech Stack

**Backend:**
- Java 21
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Swagger/OpenAPI
- Lombok
- Maven

**DevOps (Planned):**
- Docker & Docker Compose
- Kubernetes
- API Gateway (Spring Cloud Gateway)
- Service Discovery (Eureka)

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 14+

### Setup

**1. Clone Repository**
```bash
git clone https://github.com/YOUR_USERNAME/event-booking-system.git
cd event-booking-system
```

**2. Create Databases**
```sql
CREATE DATABASE user_service_db;
CREATE DATABASE event_service_db;
```

**3. Configure Services**
```bash
# User Service
cd userservice/src/main/resources
cp application.properties.example application.properties
# Edit with your database credentials

# Event Service  
cd ../../../eventservice/src/main/resources
cp application.properties.example application.properties
# Edit with your database credentials (use SAME JWT secret!)
```

**4. Start Services**

**Terminal 1 - User Service:**
```bash
cd userservice
mvn spring-boot:run
```

**Terminal 2 - Event Service:**
```bash
cd eventservice
mvn spring-boot:run
```

**5. Access Swagger UI**
- User Service: http://localhost:8084/swagger-ui.html
- Event Service: http://localhost:8081/swagger-ui.html

## 🧪 Testing the System

**1. Register a User**
```bash
curl -X POST http://localhost:8084/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstname": "Test",
    "lastname": "User"
  }'
```

**2. Login and Get JWT Token**
```bash
TOKEN=$(curl -s -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser",
    "password": "password123"
  }' | jq -r '.data.token')
```

**3. Create an Event**
```bash
curl -X POST http://localhost:8081/api/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Tech Conference 2025",
    "description": "An amazing tech conference",
    "category": "CONFERENCE",
    "startDateTime": "2025-04-20T10:00:00",
    "endDateTime": "2025-04-20T16:00:00",
    "venue": "Convention Center",
    "address": "123 Main St",
    "city": "Lagos",
    "capacity": 300,
    "price": 15000
  }'
```

**4. Browse Published Events (Public)**
```bash
curl http://localhost:8081/api/events/published
```

## 📁 Project Structure

```
event-booking-system/
├── userservice/              # User authentication service
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── eventservice/             # Event management service
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── bookingservice/           # [Coming Soon]
├── .gitignore
└── README.md
```

## 🎯 Roadmap

### Phase 1: Core Services (Current)
- [x] User Service - Authentication & user management
- [x] Event Service - Event CRUD and publishing
- [ ] Booking Service - Ticket booking and management

### Phase 2: Payment & Notifications
- [ ] Payment Service - Paystack integration
- [ ] Notification Service - Email confirmations

### Phase 3: Infrastructure
- [ ] API Gateway - Single entry point
- [ ] Service Discovery - Eureka server
- [ ] Config Server - Centralized configuration

### Phase 4: DevOps
- [ ] Docker Compose setup
- [ ] Kubernetes deployment
- [ ] CI/CD with GitHub Actions
- [ ] Cloud deployment

## 🔐 Security

- **Authentication:** JWT-based stateless authentication
- **Authorization:** Role-based access control (USER, ADMIN)
- **Password Encryption:** BCrypt hashing
- **HTTPS:** Required in production
- **Microservices Security:** Shared JWT secret for token validation

## 📊 Database Design

Each service has its own database following the Database-per-Service pattern:

- **user_service_db** - Users, roles, verification tokens
- **event_service_db** - Events, categories, status
- **booking_service_db** - Bookings, tickets (planned)

## 🤝 Contributing

This is a learning project. Contributions, issues, and feature requests are welcome!

## 👤 Author

**Senibo Don-Pedro**

## 📄 License

This project is for educational purposes.
```

---

## **STEP 3: Update .gitignore (Root Level)**

### **Your Task:** Update `/.gitignore`

```gitignore
# Maven
**/target/
**/pom.xml.tag
**/pom.xml.releaseBackup
**/pom.xml.versionsBackup
**/pom.xml.next
**/release.properties
**/dependency-reduced-pom.xml
**/buildNumber.properties
**.flattened-pom.xml

# IDE
**/.idea/
**/*.iml
**/.vscode/
**/.classpath
**/.project
**/.settings/
**/nbproject/

# Sensitive Configuration
**/application.properties
**/application-local.properties
**/application-dev.properties
**/application-prod.properties
**/.env

# Keep example files
!**/application.properties.example

# Logs
**/*.log
**/logs/
**/spring.log

# OS
.DS_Store
Thumbs.db
**/Thumbs.db

# Temporary files
**/*.tmp
**/*.bak
**/*.swp
**/*~.nib
```

---

## **STEP 4: Create application.properties.example Files**

### **User Service:**

**Create:** `userservice/src/main/resources/application.properties.example`

```properties
# ====================================
# User Service Configuration Template
# ====================================

# Application
spring.application.name=userservice
server.port=8084

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/user_service_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# JWT (Generate using: openssl rand -base64 32)
spring.app.jwtSecret=REPLACE_WITH_YOUR_SECRET_KEY_MIN_32_CHARS
spring.app.jwtExpirationMs=86400000

# Swagger
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### **Event Service:**

Already done! ✅

---

## **STEP 5: Push to GitHub**

### **Initialize Git (if not already done)**

```bash
# Navigate to project root
cd "~/Desktop/Events Project"

# Initialize git
git init

# Check status
git status
```

### **Verify .gitignore is working**

```bash
# This should NOT list application.properties files
git status | grep application.properties

# If you see application.properties (without .example), that's bad!
# If you only see .example files, that's good!
```

### **Stage Files**

```bash
# Add all files
git add .

# Verify what will be committed
git status

# You should see:
# - README.md files ✅
# - .gitignore ✅
# - application.properties.example files ✅
# - Source code ✅
#
# You should NOT see:
# - application.properties (actual config) ❌
# - target/ folders ❌
```

### **Create First Commit**

```bash
git commit -m "feat: initialize event booking microservices project

Project Setup:
- Monorepo structure for all microservices
- User Service complete with JWT authentication
- Event Service complete with dynamic search and pagination
- Standardized API response patterns
- Global exception handling
- Swagger/OpenAPI documentation
- PostgreSQL database configuration

Completed Services:
✅ User Service (Port 8084)
  - User registration & login
  - JWT-based authentication
  - Login with username or email
  - BCrypt password encryption
  - Email verification fields prepared

✅ Event Service (Port 8081)
  - Event CRUD operations
  - Dynamic search with JPA Specifications
  - Pagination support
  - Public vs protected endpoints
  - Owner-only operations
  - Business validation (dates, capacity, status)

Tech Stack:
- Java 21
- Spring Boot 3.2.0
- Spring Security + JWT
- PostgreSQL
- Swagger/OpenAPI

Next: Booking Service implementation"
