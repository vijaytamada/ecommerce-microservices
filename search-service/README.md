## 🔍 Search Service – Full-Text Product Search (Elasticsearch)

The Search Service provides fast, full-text product search powered by **Elasticsearch**. It maintains a real-time product index by consuming events from the Product and Review services via RabbitMQ. This is a **CQRS read model** — it never writes to the product catalog, only indexes.

## 📌 Purpose

This service manages:

- 🔎 Full-Text Search (name + description)
- 🗂️ Browse by Category
- 📥 Auto-indexing via RabbitMQ events (product created/updated/deleted)
- ⭐ Rating score updates from Review Service

## 🏗️ Architecture Position

```
product.events ─► Search Service ──► Elasticsearch
review.events  ─►      │
                        └── API: GET /api/search/products?q=...
```

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Data Elasticsearch
- Elasticsearch 8.x (single-node, no auth for dev)
- RabbitMQ (Pure Consumer)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
search-service/
 └── src/main/java/com/company/search_service/
     ├── controller/     → SearchController
     ├── service/        → Search & indexing logic
     ├── repository/     → ElasticsearchRepository
     ├── document/       → ProductDocument (ES document)
     ├── messaging/      → ProductEventConsumer
     ├── config/         → RabbitMQ, Swagger Config
     ├── dto/            → ApiResponse
     └── exception/      → GlobalExceptionHandler
```

## ⚙️ Configuration

```yaml
server:
  port: 8088

spring:
  application:
    name: search-service
  elasticsearch:
    uris: http://localhost:9200
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
http://localhost:8088/swagger-ui/index.html
```

## 📡 Available APIs

| Method | Endpoint                                  | Description                    |
|--------|-------------------------------------------|--------------------------------|
| GET    | `/api/search/products?q=&page=&size=`     | Full-text search with filters  |
| GET    | `/api/search/products/category/{id}`      | Browse products by category    |

## 📥 Events Consumed

| Exchange         | Routing Key   | Action                       |
|------------------|---------------|------------------------------|
| `product.events` | `product.#`   | Index / update / remove docs |
| `review.events`  | `review.submitted` | Update product rating  |

## 🚀 Start the Service

Ensure Elasticsearch is running:
```shell
docker run -d -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.13.0
```

Then:
```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8088`

## ✅ Prerequisites

| Service           | Port |
|-------------------|------|
| Elasticsearch     | 9200 |
| RabbitMQ          | 5672 |
| Eureka Server     | 8761 |
| Product Service   | 8083 |
