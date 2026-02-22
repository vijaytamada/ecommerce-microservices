## 🚀 Eureka Server – Service Discovery

This service acts as the Service Registry & Discovery Server for the E-Commerce Microservices Architecture.

It is built using:
- Java 17
- Spring Boot 3
- Spring Cloud Netflix Eureka

## 📌 Purpose

In a microservices architecture, services need to dynamically discover and communicate with each other.

The Eureka Server acts as:

- 📍 Central Service Registry
- 🔄 Dynamic Service Discovery
- ❤️ Health Monitoring Hub
- 🌐 Load Balancing Support (via clients)

All services registers themselves with Eureka for synchronous operations and for asynchronous ops we are using RabbitMQ.

## ⚙️ Configuration

```application.yml```

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false

  instance:
    prefer-ip-address: true
```

## 🧠 How It Works

1️⃣ Microservice starts  
2️⃣ It connects to Eureka Server  
3️⃣ Registers itself  
4️⃣ Sends heartbeats periodically  
5️⃣ Other services discover it dynamically  

If a service goes down:
- Eureka automatically removes it from registry

## 🚀 How to Run

#### Prerequisites
- Java 17+
- Maven 3.8+

#### Start the Server

You can start the Eureka Server using any of the following methods:

#### ▶ Option 1: Using IntelliJ IDEA

1. Open the `eureka-server` project in IntelliJ.
2. Open the main application class.
3. Right-click on the class.
4. Click **Run**.

The server will start on:
```
http://localhost:8761
```

#### ▶ Option 2: Using Maven Command

From inside the `eureka-server` folder, run:

```bash
mvn spring-boot:run
```

Once started, open:
```
http://localhost:8761
```
You will see the Eureka Dashboard.