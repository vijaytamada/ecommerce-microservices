## 🌐 API Gateway – Centralized Request Router

The API Gateway acts as the single entry point for all client requests in the E-Commerce Microservices Architecture.

It is built using:

- Java 17
- Spring Boot 3
- Spring Cloud Gateway (WebFlux)
- Redis (Rate Limiting)
- Eureka Discovery

## 📌 Purpose

In a microservices system, exposing each service directly to clients is insecure and inefficient.

The API Gateway provides:

🚪 Single Entry Point  
🔐 Centralized Authentication  
📡 Dynamic Routing  
⚡ Rate Limiting  
🔍 Service Discovery Integration  
📊 Monitoring & Health Checks  

## 🏗️ Architecture Position

```
Client
│
▼
+----------------+
|  API Gateway   |  (8080)
+----------------+
│
▼
+------------------+
|  Eureka Server   |
+------------------+
│
----------------------------------------- Other services
│          │           │           │
Auth     User       Product      Order
Service  Service    Service      Service

```

## ⚙️ Configuration

```application.yml```
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway

  # LOCAL REDIS USING DOCKER
  data:
    redis:
      host: localhost
      port: 6379

  cloud:
    gateway:
      server:
        webflux:

          discovery:
            locator:
              enabled: true
              lower-case-service-id: true

          routes:
            # Auth Service
            - id: auth-login
              uri: lb://auth-service
              predicates:
                - Path=/api/auth/**
              filters:
                - name: RequestRateLimiter
                  args:
                    redis-rate-limiter.replenishRate: 3
                    redis-rate-limiter.burstCapacity: 6
                    key-resolver: "#{@ipKeyResolver}"
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: "*"

  endpoint:
    health:
      show-details: always
```

## 🧠 How It Works

1️⃣ Client sends request to Gateway  
2️⃣ Gateway validates request  
3️⃣ Applies filters (Rate Limit)  
4️⃣ Discovers service via Eureka  
5️⃣ Routes request  
6️⃣ Returns response  

Flow:

```Client → Gateway → Filter → Eureka → Service → Response```

## 🚀 How to Run
#### Prerequisites
- Java 17+
- Maven
- Redis (Running)
- Eureka Server (Running)

#### Next Steps
- Start Redis
- Start Eureka Server
- Finally start API Gateway 

```shell
cd api-gateway
mvn spring-boot:run
```

Gateway runs on:
```shell
http://localhost:8080
```

