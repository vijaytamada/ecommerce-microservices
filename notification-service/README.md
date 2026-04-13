## 🔔 Notification Service – Multi-Event Notification Hub

The Notification Service is a pure event consumer that listens to events from all other services (Auth, Order, Payment, Shipping, Inventory) and dispatches email notifications. All sent notifications are persisted in MongoDB for history/auditing.

## 📌 Purpose

This service manages:

- 📧 Email Dispatch (via JavaMail / Mailtrap for dev)
- 📝 Notification History (MongoDB)
- 📥 Event Consumption from 5 exchanges

## 🏗️ Architecture Position

```
auth.events     ┐
order.events    │
payment.events  ├──► Notification Service ──► Email (SMTP)
shipping.events │          └──────────────► MongoDB (history)
inventory.events┘
```

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Data MongoDB
- RabbitMQ (Pure Consumer — 5 queues)
- Spring Mail (JavaMail)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
notification-service/
 └── src/main/java/com/company/notification_service/
     ├── controller/     → NotificationController (history)
     ├── service/        → NotificationService (send + save)
     ├── repository/     → MongoRepository
     ├── document/       → Notification (MongoDB document)
     ├── messaging/      → EventConsumer (all events)
     ├── config/         → RabbitMQ, Mongo, Swagger Config
     ├── dto/            → ApiResponse
     └── utils/          → HeaderUtils
```

## ⚙️ Configuration

```yaml
server:
  port: 8090

spring:
  application:
    name: notification-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/notification_db
  rabbitmq:
    host: localhost
    port: 5672
  mail:
    host: smtp.mailtrap.io
    port: 587
    username: <MAILTRAP_USERNAME>
    password: <MAILTRAP_PASSWORD>

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

## 📘 API Documentation

#### Swagger UI
```
http://localhost:8090/swagger-ui/index.html
```

## 📡 Available APIs

| Method | Endpoint                        | Description                     |
|--------|---------------------------------|---------------------------------|
| GET    | `/api/notifications/my`         | My notification history         |
| GET    | `/api/notifications/admin/all`  | All notifications (ADMIN)       |

## 📨 Events Consumed

| Exchange          | Routing Key     | Action                      |
|-------------------|-----------------|-----------------------------|
| `auth.events`     | `user.security.#` | Security alert email      |
| `order.events`    | `order.#`       | Order status emails         |
| `payment.events`  | `payment.#`     | Payment receipt/failure     |
| `shipping.events` | `shipment.#`    | Dispatch/delivery emails    |
| `inventory.events`| `inventory.#`   | Low stock alerts            |

## 🚀 Start the Service

```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8090`

## ✅ Prerequisites

| Service       | Port  |
|---------------|-------|
| MongoDB       | 27017 |
| RabbitMQ      | 5672  |
| Eureka Server | 8761  |
