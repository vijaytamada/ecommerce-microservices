## 🔐 Auth Service – Authentication & Authorization

The Auth Service is responsible for handling authentication, authorization, user security, and identity management in the E-Commerce Microservices Architecture.

It provides secure JWT-based login, registration, password management, role management, and token lifecycle handling.

## 📌 Purpose

This service manages:

- 👤 User Registration & Login
- 🔐 JWT Authentication
- ♻️ Refresh Token Management
- 📧 Email Verification & Password Reset
- 👮 Role-Based Authorization
- 📊 Login Auditing
- 📢 Event Publishing (RabbitMQ)

It acts as the central identity provider for all microservices.

## 🏗️ Architecture Position

```
Client → API Gateway → Auth Service → PostgreSQL
                     ↓
                 RabbitMQ
                     ↓
             Notification Service (Emails)
```

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- PostgreSQL
- RabbitMQ
- JavaMail (Mailtrap)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
auth-service/
 └── src/main/java/com/company/auth_service/
     ├── controller/     → REST Controllers
     ├── service/        → Business Logic
     ├── repository/     → JPA Repositories
     ├── security/       → JWT & Security Config
     ├── entity/         → Database Entities
     ├── dto/            → Request/Response DTOs
     ├── messaging/      → RabbitMQ Publishers
     ├── jobs/           → Scheduled Jobs
     ├── exception/      → Global Error Handling
     └── utils/          → Helper Utilities
```

## ⚙️ Configuration

```application.yml```   
Use ```application-example.yml``` as a template.

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service

  datasource:
    url: jdbc:postgresql://localhost:5432/<DB_NAME>
    username: <DB_USERNAME>
    password: <DB_PASSWORD>

  rabbitmq:
    host: localhost
    port: 5672

  mail:
    host: sandbox.smtp.mailtrap.io
    port: 587
    username: <MAIL_USERNAME>
    password: <MAIL_PASSWORD>

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

jwt:
  secret: CHANGE_THIS_SECRET
  access-expiry-minutes: 15
  refresh-expiry-days: 30
```

## 📘 API Documentation
#### Swagger UI
```
http://localhost:8081/swagger-ui/index.html
```

#### OpenAPI Docs
```
http://localhost:8081/v3/api-docs
```

## 📡 Available APIs

#### 🔑 Authentication

| Method | Endpoint                    | Description       |
| ------ | --------------------------- | ----------------- |
| POST   | `/api/auth/register`        | Register new user |
| POST   | `/api/auth/login`           | User login        |
| POST   | `/api/auth/refresh`         | Refresh token     |
| POST   | `/api/auth/logout`          | Logout            |
| POST   | `/api/auth/forgot-password` | Request reset     |
| POST   | `/api/auth/reset-password`  | Reset password    |


#### 👤 User Management

| Method | Endpoint                         | Description     |
| ------ | -------------------------------- | --------------- |
| POST   | `/api/auth/user/change-password` | Change password |
| POST   | `/api/auth/user/change-email`    | Change email    |
| POST   | `/api/auth/user/verify-email`    | Verify email    |


#### 👮 Admin APIs

| Method | Endpoint                             | Description |
| ------ | ------------------------------------ | ----------- |
| POST   | `/api/admin/roles/{role}`            | Create role |
| POST   | `/api/admin/users/{id}/roles/{role}` | Assign role |
| DELETE | `/api/admin/users/{id}/roles/{role}` | Remove role |
| POST   | `/api/admin/users/{id}/block`        | Block user  |
| POST   | `/api/admin/users/{id}/enable`       | Enable user |
| GET    | `/api/admin/users`                   | View users  |
| DELETE | `/api/admin/users/{id}`              | Delete user |


#### 🚀 Start the Service

You can start the Auth Service using any of the following methods:

#### ▶ Option 1: Using IntelliJ IDEA

- Open the ```auth-service``` project in IntelliJ.
- Open ```AuthServiceApplication.java```.
- Right-click and click Run.

Service runs on:
```
http://localhost:8081
```

#### ▶ Option 2: Using Maven Command

From inside the auth-service folder:
```shell
mvn spring-boot:run
```

## ✅ Prerequisites

Make sure the following are running:

| Service       | Port |
| ------------- | ---- |
| PostgreSQL    | 5432 |
| RabbitMQ      | 5672 |
| Redis         | 6379 |
| Eureka Server | 8761 |
| API Gateway   | 8080 |