# 🎟️ Event Booking Microservices Platform

A robust, event-driven microservices application for booking events, managing tickets, and handling real-time notifications. Built with **Java 21**, **Spring Boot 3**, and **Apache Kafka**, orchestrated via **Docker Compose**.

## 🚀 Key Features

* **Microservices Architecture:** Decomposed into User, Event, Booking, and Notification services.
* **API Gateway:** Centralized entry point (Port 8080) with custom **JWT Authentication** and dynamic routing.
* **Event-Driven:** Uses **Apache Kafka** to decouple services (e.g., booking a ticket triggers an asynchronous email notification).
* **Resilience:** Implements the Token Relay pattern to securely pass user context between services.
* **Containerization:** Fully Dockerized stack (Services + Postgres + Kafka + Zookeeper) for one-click deployment.
* **Security:** Stateless JWT authentication with a custom Gateway "Bouncer" filter.

---

## 🏗️ Architecture

The system consists of the following microservices:

| Service | Port | Description |
| :--- | :--- | :--- |
| **API Gateway** | `8080` | The entry point. Handles routing, security, and CORS. |
| **User Service** | `8084` | Manages registration, login, and JWT token generation. |
| **Event Service** | `8081` | Handles event creation, searching, and inventory management. |
| **Booking Service** | `8083` | Manages ticket bookings and reservation logic. |
| **Notification Service** | `N/A` | Background worker that consumes Kafka events to send emails. |

**Infrastructure:**
* **Database:** PostgreSQL (Single container with logical databases for efficiency).
* **Message Broker:** Apache Kafka (with Zookeeper).

---

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x (WebFlux & MVC)
* **Database:** PostgreSQL
* **Messaging:** Apache Kafka
* **DevOps:** Docker & Docker Compose
* **Build Tool:** Maven

---

## 🏁 Getting Started

### Prerequisites
* **Docker Desktop** (Running)
* **Java 21** & **Maven** (For building JARs)

### Installation

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/yourusername/event-booking-platform.git](https://github.com/yourusername/event-booking-platform.git)
    cd event-booking-platform
    ```

2.  **Build the JAR files**
    *This project uses a lightweight Docker build strategy. You must build the artifacts locally first.*
    ```bash
    mvn clean package -DskipTests
    ```

3.  **Launch the System**
    *Spin up all 8 containers (Services + DB + Kafka).*
    ```bash
    docker-compose up --build
    ```

4.  **Verify**
    * Wait for the logs to settle.
    * Check if the Gateway is up: [http://localhost:8080/api/events/published](http://localhost:8080/api/events/published)

---

## 🧪 Testing (Postman)

I have included a fully automated Postman collection to test the entire flow.

1.  Go to the `postman/` folder in this project.
2.  Import **both** JSON files into Postman:
    * `Event_Booking_API.postman_collection.json`
    * `Event_Booking_Env.postman_environment.json`
3.  Select the **"EventApp - Docker Local"** environment in the top right.
4.  **Run the requests in order:**
    * **Auth:** Register -> Login (This automatically saves your JWT token).
    * **Events:** Create Event (Saves the Event ID).
    * **Bookings:** Book Ticket (Uses the saved Event ID and Token).

---

## 📚 API Endpoints

All requests should be sent to the **Gateway** (`http://localhost:8080`).

### 🔐 Auth
* `POST /api/auth/register` - Register a new user
* `POST /api/auth/login` - Login and receive JWT
* `GET /api/auth/verify` - Verify email

### 📅 Events
* `GET /api/events/published` - View all public events
* `GET /api/events/search` - Search by category, city, date
* `POST /api/events` - Create a new event (Organizer only)
* `PATCH /api/events/{id}/status` - Publish or Cancel an event

### 🎫 Bookings
* `POST /api/bookings` - Book a ticket
* `GET /api/bookings/my-bookings` - View user's booking history
* `DELETE /api/bookings/{id}` - Cancel a booking

## 📧 Email Testing (MailHog)
This project uses **MailHog** to capture emails during development. You don't need a real Gmail account.

1. Perform an action that triggers an email (e.g., Register a user).
2. Open the MailHog Dashboard: [http://localhost:8025](http://localhost:8025)
3. You will see the email appear in the inbox instantly.

## ⚠️ Troubleshooting

**"Connection Refused" in Docker?**
Ensure you are using the Docker service names (e.g., `postgres`, `kafka`) in your configuration, not `localhost`. The provided `docker-compose.yml` handles this automatically.

**"401 Unauthorized"?**
Your JWT might be expired or missing. Run the "Login" request in Postman again to refresh the environment variable.

---

### 👨‍💻 Author
**DON-PEDRO SENIBO**
* [LinkedIn](https://www.linkedin.com/in/senibo-don-pedro/)
* [GitHub](https://github.com/Senibo-Don-Pedro)