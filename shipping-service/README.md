## 🚚 Shipping Service – iThink Logistics Integration

The Shipping Service manages the full shipment lifecycle. It auto-creates a shipment record when an order is confirmed via RabbitMQ, then an admin pushes it to **iThink Logistics** to receive an AWB number. Tracking is polled from iThink on a schedule and status transitions automatically publish RabbitMQ events to Order Service and Notification Service.

## 📌 Purpose

This service manages:

- 📦 Shipment Creation (auto on `order.confirmed` RabbitMQ event — DB record only, no AWB yet)
- 🚀 iThink Push (admin triggers actual iThink order creation, AWB assigned)
- 📍 Pincode Serviceability Check (public)
- 🔍 Tracking by AWB Number (public + scheduled polling every ~2 hours)
- 📄 Label & Manifest PDF generation
- ❌ Shipment Cancellation on iThink
- 📢 Event Publishing (`shipment.dispatched`, `shipment.delivered`)

## 🏗️ Architecture Position

```
RabbitMQ (order.confirmed) → Shipping Service → PostgreSQL
                                   ↓
                           Admin: POST /api/shipping
                                   ↓
                           iThink Logistics API v3
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
- RestTemplate (iThink Logistics API v3)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
shipping-service/
 └── src/main/java/com/company/shipping_service/
     ├── controller/     → ShippingController
     ├── service/        → ShippingService (interface + impl)
     ├── repository/     → ShipmentRepository, TrackingEventRepository
     ├── entity/         → Shipment, TrackingEvent
     ├── dto/            → Response DTOs
     ├── messaging/      → Consumer (order.confirmed) + ShipmentEvent publisher
     ├── client/         → IThinkClient (RestTemplate wrapper for iThink API v3)
     ├── config/         → IThinkProperties, RabbitMQConfig, Swagger Config
     └── exception/      → GlobalExceptionHandler
```

## ⚙️ Configuration

Copy `application-example.yml` to `application.yml` and fill in your values:

```yaml
server:
  port: 8089

spring:
  application:
    name: shipping-service
  datasource:
    url: jdbc:postgresql://localhost:5433/shipping_db
    username: <DB_USERNAME>
    password: <DB_PASSWORD>
  rabbitmq:
    host: localhost
    port: 5672

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

ithink:
  base-url: https://my.ithinklogistics.com
  access-token: <ITHINK_ACCESS_TOKEN>
  secret-key: <ITHINK_SECRET_KEY>
  pickup-address-id: <PICKUP_ADDRESS_ID>
  return-address-id: <RETURN_ADDRESS_ID>
  warehouse-pincode: "<WAREHOUSE_PINCODE>"
  simulate: true   # true = no real iThink calls, fake AWB generated
```

## 📘 API Documentation

#### Swagger UI
```
http://localhost:8089/swagger-ui/index.html
```

## 📡 Available APIs

### Public

| Method | Endpoint                              | Description                          |
|--------|---------------------------------------|--------------------------------------|
| GET    | `/api/shipping/check-pincode?pincode=`| Check if pincode is serviceable      |
| GET    | `/api/shipping/track/{awb}`           | Track shipment by AWB number         |

### Customer

| Method | Endpoint                              | Description                          |
|--------|---------------------------------------|--------------------------------------|
| GET    | `/api/shipping/order/{orderId}`       | Get shipment for an order            |
| GET    | `/api/shipping/{id}`                  | Get shipment by ID                   |

### Admin

| Method | Endpoint                              | Description                                    |
|--------|---------------------------------------|------------------------------------------------|
| POST   | `/api/shipping?orderId={id}`          | Push order to iThink, assign AWB               |
| POST   | `/api/shipping/{id}/track`            | Force-sync latest tracking from iThink         |
| POST   | `/api/shipping/{id}/cancel`           | Cancel shipment on iThink                      |
| POST   | `/api/shipping/{id}/documents`        | Re-fetch label & manifest PDF URLs from iThink |
| PATCH  | `/api/shipping/{id}/status`           | Manually update shipment status                |

### Internal / Cron

| Method | Endpoint                              | Description                                    |
|--------|---------------------------------------|------------------------------------------------|
| POST   | `/api/shipping/poll/sync`             | Batch-poll all due shipments from iThink       |

## 🔄 Shipment Status Flow

```
PROCESSING → MANIFESTED → DISPATCHED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
                                                                      ↘ RTO → FAILED
                                                              CANCELLED (admin/iThink cancel)
```

## 🔗 iThink Logistics Integration

All iThink API v3 calls use `POST` with the format:
```json
{
  "data": {
    "access_token": "...",
    "secret_key": "...",
    "...payload fields..."
  }
}
```

Endpoints used:
- `POST /api_v3/pincode/check.json` — serviceability
- `POST /api_v3/order/add.json` — create shipment, receive AWB
- `POST /api_v3/order/track.json` — fetch tracking events
- `POST /api_v3/order/cancel.json` — cancel shipment
- `POST /api_v3/shipping/label.json` — fetch label PDF URL
- `POST /api_v3/shipping/manifest.json` — fetch manifest PDF URL

## 📢 RabbitMQ Events

### Consumed
| Exchange        | Routing Key       | Action                           |
|-----------------|-------------------|----------------------------------|
| `order.exchange`| `order.confirmed` | Create shipment DB record        |

### Published
| Exchange           | Routing Key           | Trigger                    |
|--------------------|-----------------------|----------------------------|
| `shipping.exchange`| `shipment.dispatched` | Status → DISPATCHED        |
| `shipping.exchange`| `shipment.delivered`  | Status → DELIVERED         |

## 🚀 Start the Service

```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8089`

## ✅ Prerequisites

| Service       | Port |
|---------------|------|
| PostgreSQL    | 5433 |
| RabbitMQ      | 5672 |
| Eureka Server | 8761 |
