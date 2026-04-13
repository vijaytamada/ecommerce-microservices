## ⭐ Review Service – Product Reviews & Ratings

The Review Service handles product reviews and ratings. It stores reviews in MongoDB (document-shaped, flexible schema) and publishes events so the Search Service can update product rating scores in Elasticsearch.

## 📌 Purpose

This service manages:

- ⭐ Product Reviews (submit, update, delete)
- 📊 Rating Summary (average, breakdown 1-5 stars)
- 👍 Helpful Votes
- 🛡️ Review Moderation (ADMIN approve/reject)
- 📢 Event Publishing (review.submitted → Search Service)

## 🏗️ Architecture Position

```
Client → API Gateway → Review Service → MongoDB
                             ↓
                         RabbitMQ (review.submitted)
                             ↓
                       Search Service (updates rating in Elasticsearch)
```

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Data MongoDB
- RabbitMQ (Publisher + Consumer)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
review-service/
 └── src/main/java/com/company/review_service/
     ├── controller/     → ReviewController
     ├── service/        → Review business logic
     ├── repository/     → MongoRepository
     ├── document/       → Review (MongoDB document)
     ├── dto/            → ReviewRequest, RatingSummaryResponse
     ├── messaging/      → RabbitMQ Publisher + Consumer
     ├── config/         → RabbitMQ, Mongo Auditing, Swagger
     ├── exception/      → GlobalExceptionHandler
     └── utils/          → HeaderUtils
```

## ⚙️ Configuration

```yaml
server:
  port: 8087

spring:
  application:
    name: review-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/review_db
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
http://localhost:8087/swagger-ui/index.html
```

## 📡 Available APIs

| Method | Endpoint                                 | Description                   |
|--------|------------------------------------------|-------------------------------|
| POST   | `/api/reviews`                           | Submit a review               |
| GET    | `/api/reviews/product/{productId}`       | Reviews for a product         |
| GET    | `/api/reviews/product/{productId}/summary`| Rating summary               |
| GET    | `/api/reviews/{id}`                      | Get review by ID              |
| PUT    | `/api/reviews/{id}`                      | Update my review              |
| DELETE | `/api/reviews/{id}`                      | Delete my review              |
| POST   | `/api/reviews/{id}/helpful`              | Mark as helpful               |
| GET    | `/api/reviews/my`                        | My reviews                    |
| PATCH  | `/api/reviews/{id}/status`              | Approve/Reject review (ADMIN) |

## 🚀 Start the Service

```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8087`

## ✅ Prerequisites

| Service       | Port  |
|---------------|-------|
| MongoDB       | 27017 |
| RabbitMQ      | 5672  |
| Eureka Server | 8761  |
