## 👤 User Service – User Profile & Address Management

The User Service manages extended user profile data and addresses. Authentication and credentials are owned by the Auth Service — this service owns everything about who the user **is** (name, phone, avatar, addresses).

## 📌 Purpose

This service manages:

- 👤 User Profile (name, phone, avatar)
- 🏠 Address Book (multiple addresses with default)
- 📢 Event Consumption (auto-creates profile on registration)

## 🏗️ Architecture Position

```
Auth Service → RabbitMQ (user.created) → User Service → PostgreSQL
                                              ↑
                                         API Gateway
                                              ↑
                                           Client
```

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- RabbitMQ (Consumer)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
user-service/
 └── src/main/java/com/company/user_service/
     ├── controller/     → REST Controllers
     ├── service/        → Business Logic
     ├── repository/     → JPA Repositories
     ├── entity/         → UserProfile, Address
     ├── dto/            → Request/Response DTOs
     ├── messaging/      → RabbitMQ Consumer
     ├── config/         → RabbitMQ, Swagger Config
     ├── exception/      → Global Error Handling
     └── utils/          → HeaderUtils (X-User-Id)
```

## ⚙️ Configuration

```yaml
server:
  port: 8082

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/user_db
    username: user_app
    password: user_app_password
  rabbitmq:
    host: localhost
    port: 5672

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

## 📘 API Documentation

#### Swagger UI
```
http://localhost:8082/swagger-ui/index.html
```

## 📡 Available APIs

#### 👤 Profile

| Method | Endpoint                 | Description                   |
|--------|--------------------------|-------------------------------|
| GET    | `/api/users/me`          | Get my profile                |
| PUT    | `/api/users/me`          | Update my profile             |
| GET    | `/api/users/{id}/profile`| Get profile by ID (Admin)     |

#### 🏠 Addresses

| Method | Endpoint                              | Description           |
|--------|---------------------------------------|-----------------------|
| GET    | `/api/users/me/addresses`             | List my addresses     |
| POST   | `/api/users/me/addresses`             | Add address           |
| PUT    | `/api/users/me/addresses/{id}`        | Update address        |
| DELETE | `/api/users/me/addresses/{id}`        | Delete address        |
| PUT    | `/api/users/me/addresses/{id}/default`| Set default address   |

## 🚀 Start the Service

#### ▶ Option 1: IntelliJ IDEA
- Open `user-service` → Run `UserServiceApplication.java`

#### ▶ Option 2: Maven
```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8082`

## ✅ Prerequisites

| Service       | Port |
|---------------|------|
| PostgreSQL    | 5432 |
| RabbitMQ      | 5672 |
| Eureka Server | 8761 |
| API Gateway   | 8080 |
