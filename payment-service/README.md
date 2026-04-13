## 💳 Payment Service – Payment Processing

The Payment Service processes payments for orders. It is fully event-driven — it consumes `order.created` events from RabbitMQ, simulates payment processing (90% success rate for portfolio demo), and publishes `payment.success` or `payment.failed` events.

> In production, this would integrate with Razorpay / Stripe. The architecture is identical — only the payment gateway call changes.

## 📌 Purpose

This service manages:

- 💰 Payment Processing (simulated)
- 📜 Payment History
- 💸 Refund Initiation
- 📢 Event Publishing (payment.success, payment.failed, payment.refunded)

## 🏗️ Architecture Position

```
RabbitMQ (order.created) → Payment Service → PostgreSQL
                                 ↓
                            RabbitMQ
                          ↙           ↘
                  Order Service   Notification Service
                          ↘
                    Inventory Service
```

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Data JPA + PostgreSQL
- RabbitMQ (Consumer + Publisher)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
payment-service/
 └── src/main/java/com/company/payment_service/
     ├── controller/     → PaymentController
     ├── service/        → Payment business logic
     ├── repository/     → JPA Repositories
     ├── entity/         → Payment
     ├── dto/            → Response DTOs
     ├── messaging/      → Consumer (order events) + Event models
     ├── config/         → RabbitMQ, Swagger Config
     ├── exception/      → GlobalExceptionHandler
     └── utils/          → HeaderUtils
```

## ⚙️ Configuration

```yaml
server:
  port: 8086

spring:
  application:
    name: payment-service
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_db
    username: payment_app
    password: payment_app_password
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
http://localhost:8086/swagger-ui/index.html
```

## 📡 Available APIs

| Method | Endpoint                      | Description                |
|--------|-------------------------------|----------------------------|
| GET    | `/api/payments/{id}`          | Get payment by ID          |
| GET    | `/api/payments/order/{orderId}`| Get payment for order     |
| GET    | `/api/payments/my`            | My payment history         |
| GET    | `/api/payments/admin/all`     | All payments (ADMIN)       |
| POST   | `/api/payments/{id}/refund`   | Refund a payment (ADMIN)   |

## 🚀 Start the Service

```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8086`

## ✅ Prerequisites

| Service       | Port |
|---------------|------|
| PostgreSQL    | 5432 |
| RabbitMQ      | 5672 |
| Eureka Server | 8761 |
