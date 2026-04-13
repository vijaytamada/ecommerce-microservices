## 📦 Inventory Service – Stock Management

The Inventory Service manages stock levels per product. It handles stock reservation during order checkout to prevent overselling using a two-phase reservation pattern (Reserve → Confirm/Release).

## 📌 Purpose

This service manages:

- 📊 Stock Levels (available, reserved)
- 🔒 Stock Reservation (checkout lock)
- ✅ Reservation Confirmation (post-payment)
- 🔓 Reservation Release (order cancelled/payment failed)
- 🔔 Low Stock Alerts (publishes to RabbitMQ)

## 🏗️ Architecture Position

```
Order Service ──REST──► Inventory Service ──► RabbitMQ (inventory.low.stock)
                              │
                         PostgreSQL
                              ▲
         RabbitMQ (order.cancelled / payment.success / payment.failed)
```

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- RabbitMQ (Consumer + Publisher)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
inventory-service/
 └── src/main/java/com/company/inventory_service/
     ├── controller/     → InventoryController
     ├── service/        → Business Logic (reserve, confirm, release)
     ├── repository/     → JPA Repositories
     ├── entity/         → InventoryItem, StockReservation
     ├── dto/            → Request/Response DTOs
     ├── messaging/      → Consumer (order, payment events)
     ├── config/         → RabbitMQ, Swagger Config
     └── exception/      → GlobalExceptionHandler
```

## ⚙️ Configuration

```yaml
server:
  port: 8084

spring:
  application:
    name: inventory-service
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_db
    username: inventory_app
    password: inventory_app_password
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
http://localhost:8084/swagger-ui/index.html
```

## 📡 Available APIs

| Method | Endpoint                          | Description                        |
|--------|-----------------------------------|------------------------------------|
| POST   | `/api/inventory`                  | Create inventory entry             |
| GET    | `/api/inventory/{productId}`      | Get stock level                    |
| PUT    | `/api/inventory/{productId}/restock`| Add stock (restock)              |
| GET    | `/api/inventory/low-stock`        | List low-stock items (ADMIN)       |
| POST   | `/api/inventory/bulk-check`       | Check multiple products            |
| POST   | `/api/inventory/reserve`          | Reserve stock for an order         |
| POST   | `/api/inventory/confirm/{orderId}`| Confirm reservation (post-payment) |
| POST   | `/api/inventory/release/{orderId}`| Release reservation (cancelled)    |

## 🚀 Start the Service

```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8084`

## ✅ Prerequisites

| Service       | Port |
|---------------|------|
| PostgreSQL    | 5432 |
| RabbitMQ      | 5672 |
| Eureka Server | 8761 |
