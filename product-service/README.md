## 🛍️ Product Service – Product Catalog Management

The Product Service is the single source of truth for the product catalog. It manages products, categories, pricing, images, and custom attributes. Uses **MongoDB** for flexible document storage — ideal for products with varying attributes per category.

## 📌 Purpose

This service manages:

- 📦 Product CRUD (create, update, delete, status)
- 🗂️ Category Management (hierarchical)
- 📢 Event Publishing (product.created, product.updated, product.deleted)

## 🏗️ Architecture Position

```
Client → API Gateway → Product Service → MongoDB
                             ↓
                         RabbitMQ
                        ↙         ↘
               Search Service   Inventory Service
```

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Data MongoDB
- MongoDB
- RabbitMQ (Publisher)
- Swagger / OpenAPI
- Eureka Client

## 📁 Project Structure

```
product-service/
 └── src/main/java/com/company/product_service/
     ├── controller/     → ProductController, CategoryController
     ├── service/        → Business Logic
     ├── repository/     → Mongo Repositories
     ├── document/       → Product, Category (MongoDB docs)
     ├── dto/            → Request/Response DTOs
     ├── messaging/      → RabbitMQ Publisher
     ├── config/         → RabbitMQ, Swagger, Mongo Auditing
     ├── exception/      → Global Error Handling
     └── utils/          → HeaderUtils
```

## ⚙️ Configuration

```yaml
server:
  port: 8083

spring:
  application:
    name: product-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/product_db
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
http://localhost:8083/swagger-ui/index.html
```

## 📡 Available APIs

#### 📦 Products

| Method | Endpoint                   | Description                   |
|--------|----------------------------|-------------------------------|
| GET    | `/api/products`            | List all active products      |
| GET    | `/api/products/{id}`       | Get product by ID             |
| POST   | `/api/products`            | Create product                |
| PUT    | `/api/products/{id}`       | Update product                |
| PATCH  | `/api/products/{id}/status`| Change product status         |
| DELETE | `/api/products/{id}`       | Delete product (ADMIN)        |
| GET    | `/api/products/seller/{id}`| Products by seller            |

#### 🗂️ Categories

| Method | Endpoint                       | Description              |
|--------|--------------------------------|--------------------------|
| GET    | `/api/categories`              | List all categories      |
| GET    | `/api/categories/{id}`         | Get category             |
| POST   | `/api/categories`              | Create category (ADMIN)  |
| GET    | `/api/categories/{id}/products`| Products in category     |

## 🚀 Start the Service

```shell
mvn spring-boot:run
```

Service runs on: `http://localhost:8083`

## ✅ Prerequisites

| Service       | Port  |
|---------------|-------|
| MongoDB       | 27017 |
| RabbitMQ      | 5672  |
| Eureka Server | 8761  |
| API Gateway   | 8080  |
