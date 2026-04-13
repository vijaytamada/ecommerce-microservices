## 🛒 Order Service – Order Lifecycle Management

The Order Service is the core commerce orchestrator. It places orders, manages the full order lifecycle (PENDING → CONFIRMED → SHIPPED → DELIVERED), and coordinates with Inventory and Payment services synchronously and asynchronously.

## 📌 Purpose

This service manages:

- 📋 Order Placement (with real-time stock check via Feign)
- 🔄 Order Status Lifecycle
- 📢 Event Publishing (order.created, order.confirmed, order.cancelled, etc.)
- 📩 Event Consumption (payment & shipping events)

## 🏗️ Architecture Position

```
Client → API Gateway → Order Service ──REST──► Inventory Service
                            │         └──REST──► Product Service
                            ↓
                        RabbitMQ
                       ↙         ↘
            Payment Service   Shipping Service
                       ↘         ↙
                    Notification Service
```

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Data JPA + PostgreSQL
- Spring Cloud OpenFeign (sync calls)
- RabbitMQ (Publisher + Consumer)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
order-service/
 └── src/main/java/com/company/order_service/
     ├── controller/     → OrderController
     ├── service/        → Order business logic
     ├── repository/     → JPA Repositories
     ├── entity/         → Order, OrderItem
     ├── client/         → InventoryClient, ProductClient (Feign)
     ├── dto/            → Request/Response DTOs
     ├── messaging/      → Publisher + Consumers
     ├── config/         → RabbitMQ, Swagger Config
     ├── exception/      → GlobalExceptionHandler
     └── utils/          → HeaderUtils
```

## ⚙️ Configuration

```yaml
server:
  port: 8085

spring:
  application:
    name: order-service
  datasource:
    url: jdbc:postgresql://localhost:5432/order_db
    username: order_app
    password: order_app_password
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
http://localhost:8085/swagger-ui/index.html
```

## 📡 Available APIs

| Method | Endpoint                | Description                  |
|--------|-------------------------|------------------------------|
| POST   | `/api/orders`           | Place a new order            |
| GET    | `/api/orders/{id}`      | Get order details            |
| GET    | `/api/orders`           | My orders (paginated)        |
| DELETE | `/api/orders/{id}/cancel`| Cancel order                |
| GET    | `/api/orders/admin/all` | All orders (ADMIN)           |
| PATCH  | `/api/orders/{id}/status`| Update order status (ADMIN) |

## 🔄 Order Status Flow

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
                ↘
             CANCELLED
```

## 🚀 Start the Service

```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8085`

## ✅ Prerequisites

| Service           | Port |
|-------------------|------|
| PostgreSQL        | 5432 |
| RabbitMQ          | 5672 |
| Eureka Server     | 8761 |
| Inventory Service | 8084 |
| Product Service   | 8083 |
