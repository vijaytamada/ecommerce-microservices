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


| Service Name                 | Description                         | Status        |
| ---------------------------- | ----------------------------------- | ------------- |
| API Gateway                  | Entry point for all client requests | ✅ Implemented |
| Eureka Server                | Service Discovery                   | ✅ Implemented |
| Auth Service                 | Authentication & JWT handling       | ✅ Implemented |
| User Service                 | User profile management             | 🚧 Planned    |
| Product Service              | Product catalog                     | 🚧 Planned    |
| Inventory Service            | Stock management                    | 🚧 Planned    |
| Order Service                | Order processing                    | 🚧 Planned    |
| Payment Service              | Payment handling (Razorpay)         | 🚧 Planned    |
| Review & Ratings Service     | Product reviews                     | 🚧 Planned    |
| Search Service               | Product search                      | 🚧 Planned    |
| Shipping / Logistics Service | Order delivery (ShipRocket)         | 🚧 Planned    |
| Notification Service         | Email/SMS/Push notifications        | 🚧 Planned    |

- ✅ Currently Implemented: API Gateway, Eureka Server, Auth Service
- 🚧 Under Development: Remaining services


## 🛠️ Technology Stack
#### Backend
- Java
- Spring Boot
- Spring Cloud
- Spring Security
- JWT Authentication
- PostgreSQL
- Redis

#### Messaging & Async Processing
- RabbitMQ
- External Integrations
- Razorpay (Payments)
- ShipRocket (Logistics)
- Mailtrap SMTP

### DevOps & Tools
- Git & GitHub
- Maven
- Docker (Planned)
- Postman
- IntelliJ IDEA

## 📁 Repository Structure
```
ecommerce-microservices/
│
├── api-gateway/
├── eureka-server/
├── auth-service/
├── user-service/
├── product-service/
├── order-service/
├── inventory-service/
├── payment-service/
├── notification-service/
├── rating-service/
├── search-Service/
├── shipping-Service
└── README.md (This file)
```

Each service contains its own:
- Source code
- Configuration
- Database setup
- Individual README for setup

## 📈 Key Features

✔ Microservices Architecture  
✔ JWT Authentication  
✔ Service Discovery  
✔ Centralized Routing  
✔ Event-Driven Messaging  
✔ Distributed Caching  
✔ External API Integration  
✔ Scalable Design  

