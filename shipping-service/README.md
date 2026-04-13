## 🚚 Shipping Service – Shipment & Logistics Tracking

The Shipping Service creates and tracks shipments. It auto-creates a shipment when an order is confirmed, assigns a tracking number, and allows status progression from PROCESSING → DISPATCHED → DELIVERED. Status changes trigger RabbitMQ events that update the Order Service.

## 📌 Purpose

This service manages:

- 📦 Shipment Creation (auto on order.confirmed event)
- 🔍 Tracking by Tracking Number (public endpoint)
- 📍 Tracking History (per shipment)
- 📢 Event Publishing (shipment.dispatched, shipment.delivered)

## 🏗️ Architecture Position

```
RabbitMQ (order.confirmed) → Shipping Service → PostgreSQL
                                   ↓
                               RabbitMQ
                             ↙           ↘
                     Order Service   Notification Service
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
shipping-service/
 └── src/main/java/com/company/shipping_service/
     ├── controller/     → ShippingController
     ├── service/        → Shipping business logic
     ├── repository/     → JPA Repositories
     ├── entity/         → Shipment, TrackingEvent
     ├── dto/            → Response DTOs
     ├── messaging/      → Consumer (order events) + Event models
     ├── config/         → RabbitMQ, Swagger Config
     └── exception/      → GlobalExceptionHandler
```

## ⚙️ Configuration

```yaml
server:
  port: 8089

spring:
  application:
    name: shipping-service
  datasource:
    url: jdbc:postgresql://localhost:5432/shipping_db
    username: shipping_app
    password: shipping_app_password
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
http://localhost:8089/swagger-ui/index.html
```

## 📡 Available APIs

| Method | Endpoint                              | Description                       |
|--------|---------------------------------------|-----------------------------------|
| GET    | `/api/shipping/order/{orderId}`       | Get shipment for order            |
| GET    | `/api/shipping/{id}`                  | Get shipment by ID                |
| GET    | `/api/shipping/track/{trackingNumber}`| Public tracking by number         |
| PATCH  | `/api/shipping/{id}/status`           | Update shipment status (ADMIN)    |

## 🔄 Shipment Status Flow

```
PROCESSING → DISPATCHED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
```

## 🚀 Start the Service

```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8089`

## ✅ Prerequisites

| Service       | Port |
|---------------|------|
| PostgreSQL    | 5432 |
| RabbitMQ      | 5672 |
| Eureka Server | 8761 |
