## 🛒 E-Commerce Microservices Architecture (Spring Boot)

A scalable and production-oriented E-Commerce Microservices System built using Java & Spring Boot, following modern cloud-native and distributed system principles.

This project is designed to demonstrate real-world microservices architecture and is being developed as part of my professional portfolio.


## 📌 Project Objective
The goal of this project is to:
- Build a fully decoupled e-commerce platform
- Follow industry best practices
- Implement service discovery, API gateway, authentication, messaging, and caching
- Demonstrate system design and backend engineering skills.

## 🏗️ System Architecture

The platform follows a microservices-based architecture with:

- Centralized API Gateway
- Service Discovery
- Independent databases per service
- Event-driven communication
- External integrations

## 📦 Microservices Overview


| Service Name                 | Description                              | Port | Status         |
| ---------------------------- | ---------------------------------------- | ---- | -------------- |
| API Gateway                  | Entry point for all client requests      | 8080 | ✅ Implemented |
| Eureka Server                | Service Discovery                        | 8761 | ✅ Implemented |
| Auth Service                 | Authentication & JWT handling            | 8081 | ✅ Implemented |
| User Service                 | User profile & address management        | 8082 | ✅ Implemented |
| Product Service              | Product catalog & categories             | 8083 | ✅ Implemented |
| Inventory Service            | Stock management & reservations          | 8084 | ✅ Implemented |
| Order Service                | Order processing & lifecycle             | 8085 | ✅ Implemented |
| Payment Service              | Simulated payment processing             | 8086 | ✅ Implemented |
| Review & Ratings Service     | Product reviews & rating summaries       | 8087 | ✅ Implemented |
| Search Service               | Elasticsearch-powered product search     | 8088 | ✅ Implemented |
| Shipping / Logistics Service | Shipment tracking & delivery             | 8089 | ✅ Implemented |
| Notification Service         | Email notifications via SMTP             | 8090 | ✅ Implemented |

- ✅ All 12 services implemented and integrated


## 🛠️ Technology Stack

#### Backend
- Java 17
- Spring Boot 3.5.x
- Spring Cloud (Eureka, Gateway, OpenFeign)
- Spring Security + JWT Authentication
- PostgreSQL (Auth, User, Inventory, Order, Payment, Shipping)
- MongoDB (Product, Review, Notification)
- Elasticsearch (Search)
- Redis (Rate Limiting)

#### Messaging & Async Processing
- RabbitMQ (Topic Exchanges, Event-Driven Architecture)

#### External Integrations
- Mailtrap SMTP (Email Notifications)

#### DevOps & Tools
- Git & GitHub
- Maven
- Docker & Docker Compose
- Postman
- IntelliJ IDEA

## 📁 Repository Structure
```
ecommerce-microservices/
│
├── api-gateway/          # Port 8080 — JWT validation, rate limiting, routing
├── eureka-server/        # Port 8761 — Service discovery
├── auth-service/         # Port 8081 — Registration, login, JWT issuance
├── user-service/         # Port 8082 — Profiles, addresses
├── product-service/      # Port 8083 — Products, categories
├── inventory-service/    # Port 8084 — Stock, reservations
├── order-service/        # Port 8085 — Order lifecycle
├── payment-service/      # Port 8086 — Payment processing
├── review-service/       # Port 8087 — Reviews, ratings
├── search-service/       # Port 8088 — Elasticsearch product search
├── shipping-service/     # Port 8089 — Shipment tracking
├── notification-service/ # Port 8090 — Email notifications
├── docker-compose.yml    # Full stack orchestration
└── README.md
```

Each service contains its own:
- Source code with layered architecture (controller → service → repository)
- Independent database configuration
- RabbitMQ event publishing/consuming
- Swagger/OpenAPI documentation at `/swagger-ui.html`
- Individual README for setup instructions

## 🔄 Event-Driven Flow

```
User Registers → Auth Service → [auth.events] → User Service (creates profile)
                                              → Notification Service (welcome email)

Place Order → Order Service → [Feign] → Inventory Service (reserve stock)
                           → [Feign] → Product Service (snapshot product details)
                           → [order.events] → Payment Service (process payment)
                                           → Notification Service (order confirmation)

Payment Success → [payment.events] → Order Service (confirm order)
                                   → Inventory Service (confirm reservation)
                                   → Shipping Service (create shipment)
                                   → Notification Service (payment confirmation)

Shipment Update → [shipping.events] → Order Service (update status)
                                    → Notification Service (delivery update)

Product Created/Updated → [product.events] → Search Service (index in Elasticsearch)
```

## 📈 Key Features

✔ Microservices Architecture  
✔ JWT Authentication (validated at Gateway, forwarded as headers)  
✔ Service Discovery (Eureka)  
✔ Centralized API Gateway with rate limiting  
✔ Event-Driven Messaging (RabbitMQ Topic Exchanges)  
✔ Synchronous inter-service calls (OpenFeign)  
✔ Two-phase stock reservation (Reserve → Confirm/Release)  
✔ Full-text product search (Elasticsearch)  
✔ Email notifications (SMTP)  
✔ Docker Compose for full-stack local deployment  
✔ Swagger/OpenAPI docs on every service  

