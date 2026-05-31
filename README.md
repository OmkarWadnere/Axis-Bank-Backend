# Axis Bank Backend Services

A robust, secure, and highly scalable banking backend system built using **Java**, **Spring Boot**, and a **Microservices Architecture**. This project simulates core banking operations, including account management, secure transaction processing, fund transfers, and real-time auditing, adhering to enterprise-level financial software standards.

---

## 🚀 Architecture Overview

The system is designed as a decentralized suite of microservices interacting asynchronously and synchronously to process high-throughput banking operations.

* **API Gateway:** Single entry point for all clients, handling request routing, rate limiting, and centralized security.
* **Service Discovery (Netflix Eureka):** Dynamically registers and discovers microservice instances for seamless load balancing.
* **Account Management Service:** Manages customer profiles, KYC statuses, and savings/current account lifecycles.
* **Transaction Service:** Handles secure fund transfers (NEFT, RTGS, IMPS), deposits, and withdrawals using distributed transaction patterns to ensure data integrity.
* **Notification Service:** Dispatches real-time transaction alerts via SMS/Email (simulated asynchronously).

---

## 🛠️ Tech Stack & Tools

* **Backend:** Java 11 / 17, Spring Boot, Spring Cloud (Gateway, Eureka, OpenFeign)
* **Security:** Spring Security, JWT (JSON Web Tokens), OAuth2
* **Data & Persistence:** Spring Data JPA, Hibernate, MySQL / PostgreSQL
* **Resilience & Monitoring:** Resilience4j (Circuit Breaker, Rate Limiter), OpenTelemetry / Micrometer
* **Build & Deployment:** Maven, Docker, CI/CD (Jenkins/GitHub Actions)

---

## 🔒 Key Features

### 1. Financial Data Integrity
* Implements strict transaction management configurations (`@Transactional`) to prevent partial failures during cross-account transfers.
* Optimistic/Pessimistic locking mechanisms to protect balance updates from race conditions during concurrent requests.

### 2. Distributed Resilience
* **Fault Tolerance:** Integrates **Resilience4j** Circuit Breakers to isolate failing downstream services (e.g., Notification Service) without bringing down core transaction flows.
* **Inter-Service Communication:** Utilizes **Spring Cloud OpenFeign** for clean, declarative REST client communication between microservices.

### 3. Enterprise Security
* Role-Based Access Control (RBAC) ensuring only authorized bank personnel or validated customers can hit specific endpoints.
* State-independent token validation via JWT at the API Gateway level.

---

## ⚙️ Getting Started

### Prerequisites
* JDK 11 or higher
* Maven 3.6+
* Docker (Optional, for database/containerization)

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/OmkarWadnere/Axis-Bank-Backend.git](https://github.com/OmkarWadnere/Axis-Bank-Backend.git)
   cd Axis-Bank-Backend
