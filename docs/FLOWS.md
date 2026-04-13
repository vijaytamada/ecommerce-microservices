# E-Commerce Microservices — Architecture & Flow Reference

> This document covers every major business flow, API request lifecycle, and event-driven interaction across all 12 services.

---

## Table of Contents

1. [System Architecture Overview](#1-system-architecture-overview)
2. [API Gateway — Request Lifecycle](#2-api-gateway--request-lifecycle)
3. [Auth Flow — Register & Login](#3-auth-flow--register--login)
4. [User Profile Flow](#4-user-profile-flow)
5. [Product Management Flow](#5-product-management-flow)
6. [Order Placement Flow (Core)](#6-order-placement-flow-core)
7. [Payment Flow](#7-payment-flow)
8. [Shipping & Tracking Flow](#8-shipping--tracking-flow)
9. [Review & Ratings Flow](#9-review--ratings-flow)
10. [Search Indexing Flow](#10-search-indexing-flow)
11. [Notification Fan-Out Flow](#11-notification-fan-out-flow)
12. [Complete End-to-End Order Journey](#12-complete-end-to-end-order-journey)
13. [API Reference — All Services](#13-api-reference--all-services)
14. [RabbitMQ Exchange & Queue Map](#14-rabbitmq-exchange--queue-map)

---

## 1. System Architecture Overview

```
                         ┌──────────────────────────────────────────────────────┐
                         │                  CLIENT (Browser/Mobile/Postman)     │
                         └─────────────────────────┬────────────────────────────┘
                                                   │ HTTP
                                                   ▼
                    ┌──────────────────────────────────────────────────────────┐
                    │                    API GATEWAY  :8080                    │
                    │  • JWT Validation (Spring Cloud Gateway filter)          │
                    │  • Rate Limiting (Redis token bucket per IP)             │
                    │  • Route to downstream via Eureka lb://service-name      │
                    │  • Injects X-User-Id, X-User-Email, X-User-Roles        │
                    └───────┬───────────────────────────────────────────┬──────┘
                            │ Service Discovery                          │
                            ▼                                            │
              ┌─────────────────────────┐                               │
              │   EUREKA SERVER  :8761  │◄──── All services register    │
              └─────────────────────────┘                               │
                                                                        │
          ┌─────────────────────────────────────────────────────────────┼──────┐
          │                      MICROSERVICES LAYER                    │      │
          │                                                             ▼      │
          │  ┌──────────┐  ┌──────────┐  ┌─────────────┐  ┌─────────────────┐│
          │  │   Auth   │  │  User    │  │   Product   │  │   Inventory     ││
          │  │  :8081   │  │  :8082   │  │   :8083     │  │   :8084         ││
          │  │PostgreSQL│  │PostgreSQL│  │  MongoDB    │  │  PostgreSQL     ││
          │  └────┬─────┘  └────┬─────┘  └──────┬──────┘  └────────┬────────┘│
          │       │             │                │                   │         │
          │  ┌────┴─────┐  ┌───┴──────┐  ┌─────┴──────┐  ┌────────┴────────┐│
          │  │  Order   │  │ Payment  │  │  Shipping  │  │    Review       ││
          │  │  :8085   │  │  :8086   │  │   :8089    │  │    :8087        ││
          │  │PostgreSQL│  │PostgreSQL│  │ PostgreSQL  │  │   MongoDB       ││
          │  └────┬─────┘  └────┬─────┘  └──────┬──────┘  └────────┬────────┘│
          │       │             │                │                   │         │
          │  ┌────┴─────┐  ┌───┴──────────────────────────────────┐          │
          │  │  Search  │  │          Notification  :8090          │          │
          │  │  :8088   │  │            MongoDB                    │          │
          │  │Elastic   │  └───────────────────────────────────────┘          │
          │  └──────────┘                                                      │
          └────────────────────────────────────────────────────────────────────┘
                                         │
                         ┌───────────────┴───────────────┐
                         │       RabbitMQ  :5672         │
                         │   7 Topic Exchanges            │
                         │   auth.events                 │
                         │   product.events              │
                         │   order.events                │
                         │   payment.events              │
                         │   shipping.events             │
                         │   inventory.events            │
                         │   review.events               │
                         └───────────────────────────────┘
```

---

## 2. API Gateway — Request Lifecycle

Every client request passes through the gateway before reaching any service.

```
┌──────────┐          ┌─────────────────────────────────────────────┐
│  CLIENT  │          │               API GATEWAY                   │
└────┬─────┘          └─────────────────────────────────────────────┘
     │
     │  1. HTTP Request (with or without Authorization: Bearer <token>)
     ├─────────────────────────────────────────────────────────────────►
     │
     │                 2. Is this a public route?
     │                    (POST /api/auth/register, /login, /refresh,
     │                     GET  /api/search/products, /api/products/{id})
     │                         │
     │                    YES ─┤──► Skip JWT filter → route directly
     │                    NO  ─┤
     │                         │
     │                 3. Extract Bearer token from Authorization header
     │                         │
     │                 4. Validate JWT signature + expiry
     │                         │
     │                    INVALID → 401 Unauthorized (returned to client)
     │                    VALID   ─►
     │                         │
     │                 5. Rate limit check (Redis token bucket per IP)
     │                         │
     │                    EXCEEDED → 429 Too Many Requests
     │                    OK       ─►
     │                         │
     │                 6. Inject headers into downstream request:
     │                    X-User-Id    = userId from JWT claims
     │                    X-User-Email = email from JWT claims
     │                    X-User-Roles = roles from JWT claims
     │                         │
     │                 7. Route via Eureka (lb://service-name)
     │
     │  8. Response from downstream service (ApiResponse<T>)
     │◄─────────────────────────────────────────────────────────────────
```

**Each downstream service** reads identity from headers via `HeaderUtils`:
```java
UUID userId = UUID.fromString(
    RequestContextHolder.currentRequestAttributes()
        .getAttribute("X-User-Id", ...)
);
```
No service has Spring Security or JWT validation — the Gateway handles it all.

---

## 3. Auth Flow — Register & Login

### 3a. Registration

```
Client                 Auth Service               RabbitMQ
  │                        │                          │
  │  POST /api/auth/       │                          │
  │  register              │                          │
  │  {email,password,name} │                          │
  ├───────────────────────►│                          │
  │                        │  Validate request        │
  │                        │  Check email not taken   │
  │                        │  Hash password (BCrypt)  │
  │                        │  Assign ROLE_USER        │
  │                        │  Save to auth_db         │
  │                        │  Generate email token    │
  │                        │  Send verify email (SMTP)│
  │                        │                          │
  │                        │  Publish user.created ──►│────► User Service
  │                        │                          │      (create profile)
  │                        │                          │────► Notification Svc
  │                        │                          │      (welcome email)
  │  200 {userId, email}   │                          │
  │◄───────────────────────│                          │
```

### 3b. Login

```
Client                 Auth Service
  │                        │
  │  POST /api/auth/login  │
  │  {email, password}     │
  ├───────────────────────►│
  │                        │  Load user from auth_db
  │                        │  Verify BCrypt password
  │                        │  Generate Access Token  (JWT, 15 min)
  │                        │  Generate Refresh Token (JWT, 30 days)
  │                        │  Save refresh token hash to DB
  │                        │
  │  200 {accessToken,     │
  │       refreshToken}    │
  │◄───────────────────────│

  ┌──── JWT Payload (Access Token) ────────────────────┐
  │  sub: userId (UUID)                                │
  │  email: user@example.com                           │
  │  roles: ["ROLE_USER"]                              │
  │  exp: now + 15 minutes                             │
  └────────────────────────────────────────────────────┘
```

### 3c. Token Refresh

```
Client                 Auth Service
  │                        │
  │  POST /api/auth/refresh│
  │  {refreshToken}        │
  ├───────────────────────►│
  │                        │  Validate refresh token JWT
  │                        │  Verify token hash in DB (not revoked)
  │                        │  Issue new Access Token
  │                        │  Rotate Refresh Token (old revoked)
  │                        │
  │  200 {newAccessToken,  │
  │       newRefreshToken} │
  │◄───────────────────────│
```

---

## 4. User Profile Flow

```
Client         API Gateway          User Service           RabbitMQ
  │                │                     │                     │
  │                │  ◄── Auth Service publishes user.created  │
  │                │                     │◄────────────────────│
  │                │                     │  Create UserProfile │
  │                │                     │  {userId, email,    │
  │                │                     │   firstName, ...}   │
  │                │                     │  Save to user_db    │
  │                │                     │                     │
  │ GET /api/users/me                    │                     │
  │ Authorization: Bearer <token>        │                     │
  ├───────────────►│                     │                     │
  │                │ Validate JWT        │                     │
  │                │ Inject X-User-Id    │                     │
  │                ├────────────────────►│                     │
  │                │                     │  Read X-User-Id     │
  │                │                     │  Fetch from user_db │
  │ 200 {profile}  │                     │                     │
  │◄───────────────┤◄────────────────────│                     │
  │                │                     │                     │
  │ POST /api/users/me/addresses         │                     │
  │ {street, city, isDefault: true}      │                     │
  ├───────────────►│────────────────────►│                     │
  │                │                     │  clearDefaultForUser│
  │                │                     │  Save new address   │
  │ 201 {address}  │                     │                     │
  │◄───────────────┤◄────────────────────│                     │
```

---

## 5. Product Management Flow

### 5a. Create Product (Seller/Admin)

```
Client         API Gateway        Product Service          RabbitMQ
  │                │                    │                     │
  │ POST /api/products                  │                     │
  │ {name, price, categoryId, ...}      │                     │
  ├───────────────►│                    │                     │
  │                │ Validate JWT       │                     │
  │                ├───────────────────►│                     │
  │                │                    │  Validate category  │
  │                │                    │  Save to MongoDB    │
  │                │                    │  Publish ──────────►│──► Search Svc
  │                │                    │  product.created    │    (index)
  │ 201 {product}  │                    │                     │
  │◄───────────────┤◄───────────────────│                     │
```

### 5b. Product Events → Search Index

```
Product Service                 RabbitMQ               Search Service
      │                            │                        │
      │──── product.created ──────►│───► search.product.q ─►│
      │──── product.updated ──────►│                        │  Upsert document
      │──── product.deleted ──────►│                        │  in Elasticsearch
      │──── product.status.changed►│                        │
```

---

## 6. Order Placement Flow (Core)

This is the most complex flow — it involves 3 synchronous Feign calls and then event-driven processing.

```
Client      API Gateway      Order Service      Inventory Svc      Product Svc
  │              │                │                   │                │
  │ POST /api/orders             │                   │                │
  │ {items:[{productId,qty}],    │                   │                │
  │  shippingAddress}            │                   │                │
  ├─────────────►│               │                   │                │
  │              │ Validate JWT  │                   │                │
  │              │ Inject        │                   │                │
  │              │ X-User-Id     │                   │                │
  │              ├──────────────►│                   │                │
  │              │               │                   │                │
  │              │               │ 1. BULK CHECK      │                │
  │              │               │   [Feign]          │                │
  │              │               ├──────────────────►│                │
  │              │               │ POST /inventory/  │                │
  │              │               │ bulk-check        │                │
  │              │               │ {productIds}      │                │
  │              │               │◄──────────────────│                │
  │              │               │ {p1:true,p2:false}│                │
  │              │               │                   │                │
  │              │               │ 2. SNAPSHOT PRODUCT DETAILS        │
  │              │               │   [Feign] for each product         │
  │              │               ├───────────────────────────────────►│
  │              │               │ GET /products/{id}                 │
  │              │               │◄───────────────────────────────────│
  │              │               │ {name, price, ...}                 │
  │              │               │                                    │
  │              │               │ 3. RESERVE STOCK  │                │
  │              │               │   [Feign]         │                │
  │              │               ├──────────────────►│                │
  │              │               │ POST /inventory/  │                │
  │              │               │ reserve           │                │
  │              │               │ {orderId,items}   │                │
  │              │               │◄──────────────────│                │
  │              │               │ StockReservation  │                │
  │              │               │ saved (PENDING)   │                │
  │              │               │                   │                │
  │              │               │ 4. Save Order (PENDING status)     │
  │              │               │    Serialize shippingAddress → JSON│
  │              │               │    Calculate totalAmount           │
  │              │               │                                    │
  │              │               │ 5. Publish order.created           │
  │              │               │   ────────────────────────────────►│ RabbitMQ
  │              │               │                                    │
  │ 201 {order}  │               │                                    │
  │◄─────────────┤◄──────────────│                                    │
```

**What happens next is fully event-driven:**

```
RabbitMQ (order.created)
    │
    ├──► Payment Service (payment.order.created.q)
    │       └── auto-processes payment (see Flow 7)
    │
    └──► Notification Service (notification.order.q)
            └── sends "Order Received" email
```

---

## 7. Payment Flow

Payment is **fully automated** — triggered by the `order.created` event, no manual client call needed.

```
RabbitMQ          Payment Service                   RabbitMQ
(order.created)        │                          (payment.events)
     │                 │                                │
     ├────────────────►│                                │
     │                 │  Extract orderId, userId,      │
     │                 │  totalAmount from event        │
     │                 │                                │
     │                 │  ┌─────────────────────────┐  │
     │                 │  │  simulate=true (default) │  │
     │                 │  │  Math.random() > 0.1     │  │
     │                 │  │  → 90% SUCCESS           │  │
     │                 │  │  → 10% FAILED            │  │
     │                 │  └─────────────────────────┘  │
     │                 │  (simulate=false → Razorpay)   │
     │                 │                                │
     │              ┌──┴── SUCCESS ─────────────────────┼──► payment.success
     │              │  Save Payment(SUCCESS, TXN-XXXX)  │
     │              │                                   │
     │              └──── FAILED ──────────────────────►│──► payment.failed
     │                   Save Payment(FAILED)            │
     │
     ▼
RabbitMQ (payment.events) fan-out:

payment.success ──►  Order Service        → status: CONFIRMED
                ──►  Inventory Service    → confirm reservation
                ──►  Shipping Service     → create shipment
                ──►  Notification Service → "Payment Successful" email

payment.failed  ──►  Order Service        → status: CANCELLED
                ──►  Inventory Service    → release reservation
                ──►  Notification Service → "Payment Failed" email
```

### Manual Payment APIs (for admin/queries)

```
GET  /api/payments/{id}           → get payment details
GET  /api/payments/order/{orderId}→ get payment by order
GET  /api/payments/my             → my payment history
GET  /api/payments/admin/all      → all payments (ADMIN)
POST /api/payments/{id}/refund    → refund (ADMIN)
  └── saves REFUNDED status → publishes payment.refunded event
```

---

## 8. Shipping & Tracking Flow

### 8a. Auto-Shipment Creation (Event-Driven)

```
RabbitMQ             Shipping Service                  RabbitMQ
(order.confirmed)         │                         (shipping.events)
     │                    │                                │
     ├───────────────────►│                               │
     │                    │  Generate tracking number:    │
     │                    │  TRK-XXXXXXXXXX               │
     │                    │  (simulate=false → ShipRocket)│
     │                    │                               │
     │                    │  Create Shipment:             │
     │                    │  ┌─────────────────────────┐  │
     │                    │  │ status: PROCESSING       │  │
     │                    │  │ carrier: LOCAL_COURIER   │  │
     │                    │  │ estimatedDelivery: +5d   │  │
     │                    │  │ trackingEvent: "Received  │  │
     │                    │  │  at Warehouse"            │  │
     │                    │  └─────────────────────────┘  │
     │                    │  Save to shipping_db           │
```

### 8b. Status Update Flow (Admin manually progresses)

```
Admin                API Gateway        Shipping Service       RabbitMQ
  │                       │                   │                   │
  │ PATCH /api/shipping   │                   │                   │
  │ /{id}/status          │                   │                   │
  │ {status: DISPATCHED}  │                   │                   │
  ├──────────────────────►│──────────────────►│                   │
  │                       │                   │ Update status      │
  │                       │                   │ Add TrackingEvent  │
  │                       │                   │                    │
  │                       │                   │ If DISPATCHED:     │
  │                       │                   ├──────────────────►│──► shipment.dispatched
  │                       │                   │                    │
  │                       │                   │ If DELIVERED:      │
  │                       │                   │ Set actualDelivery │
  │                       │                   ├──────────────────►│──► shipment.delivered
  │                       │                   │                    │
  │ 200 {shipment}        │                   │                    │
  │◄──────────────────────┤◄──────────────────│                    │

shipment.dispatched ──► Order Service        → status: SHIPPED
                   ──► Notification Service  → "Your order is on the way!" email

shipment.delivered  ──► Order Service        → status: DELIVERED
                   ──► Notification Service  → "Order Delivered" email
```

### 8c. Tracking API (Public — no JWT required)

```
GET /api/shipping/track/{trackingNumber}
    → returns shipment + full tracking event history (no auth needed)
GET /api/shipping/order/{orderId}
    → returns shipment for a given order
```

---

## 9. Review & Ratings Flow

### 9a. Submit a Review

```
Client        API Gateway      Review Service      Order Service    RabbitMQ
  │                │                │                   │               │
  │ POST /api/reviews              │                   │               │
  │ {productId, orderId,           │                   │               │
  │  rating, title, body}          │                   │               │
  ├──────────────►│                │                   │               │
  │               │ Validate JWT   │                   │               │
  │               ├───────────────►│                   │               │
  │               │                │ Read X-User-Id    │               │
  │               │                │                   │               │
  │               │                │ Verify via Feign: │               │
  │               │                ├──────────────────►│               │
  │               │                │ GET /orders/{id}  │               │
  │               │                │◄──────────────────│               │
  │               │                │ Check: order.userId == userId     │
  │               │                │        order.status == DELIVERED  │
  │               │                │        no existing review          │
  │               │                │                                   │
  │               │                │ Save Review (status: PENDING)     │
  │               │                │                                   │
  │               │                ├──────────────────────────────────►│──► review.submitted
  │               │                │                                   │
  │ 201 {review}  │                │                                   │
  │◄──────────────┤◄───────────────│                                   │

review.submitted ──► Search Service → update averageRating in Elasticsearch
                ──► Notification Service → notify seller (if wired)
```

### 9b. Rating Summary

```
GET /api/reviews/product/{productId}/summary
→ returns:
   {
     averageRating: 4.2,
     totalReviews: 128,
     ratingBreakdown: {5: 60, 4: 40, 3: 15, 2: 8, 1: 5}
   }
```

### 9c. Review Moderation (Admin)

```
PATCH /api/reviews/{id}/status
Body: {status: "APPROVED"} or {status: "REJECTED"}

Only APPROVED reviews are visible to public GET /api/reviews/product/{id}
```

---

## 10. Search Indexing Flow

Search Service is a **pure read model** — it never writes to other services, only consumes events.

```
Events → Search Service → Elasticsearch Index ("products")

┌────────────────────────────────────────────────────────────┐
│  WRITE PATH (event-driven indexing)                        │
│                                                            │
│  product.created       → index new ProductDocument        │
│  product.updated       → update document fields           │
│  product.status.changed→ update status field              │
│  product.deleted       → delete document from index       │
│  review.submitted      → update averageRating,            │
│                           totalReviews in document         │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│  READ PATH (Elasticsearch queries)                         │
│                                                            │
│  GET /api/search/products?q=laptop&page=0&size=10          │
│  → multi-field match on name + description                 │
│  → if q is empty: return all (findAll)                     │
│                                                            │
│  GET /api/search/products/category/{categoryId}            │
│  → filter by categoryId field                              │
│  → results sorted by relevance score                       │
└────────────────────────────────────────────────────────────┘
```

**Elasticsearch Document Structure:**
```json
{
  "id": "product-mongo-id",
  "name": "Apple MacBook Pro",
  "description": "M3 chip, 16GB RAM...",
  "categoryId": "cat-123",
  "categoryName": "Laptops",
  "price": 1299.99,
  "status": "ACTIVE",
  "averageRating": 4.5,
  "totalReviews": 89
}
```

---

## 11. Notification Fan-Out Flow

Notification Service listens to **5 exchanges** and sends emails for every key lifecycle event.

```
Exchange           Routing Key              Email Sent
─────────────────────────────────────────────────────────────
auth.events        user.created         →  "Welcome to E-Commerce!"
auth.events        user.disabled        →  "Account disabled"

order.events       order.created        →  "Order #XX received"
order.events       order.cancelled      →  "Order #XX cancelled"

payment.events     payment.success      →  "Payment confirmed – TXN-XXXX"
payment.events     payment.failed       →  "Payment failed for order #XX"
payment.events     payment.refunded     →  "Refund initiated"

shipping.events    shipment.dispatched  →  "Your order is on the way!"
shipping.events    shipment.delivered   →  "Order delivered!"

inventory.events   inventory.low.stock  →  "Low stock alert: <product>"
```

**Email is attempted via SMTP (Mailtrap). Notification is saved to MongoDB regardless of send success/failure, with status SENT or FAILED.**

```
GET /api/notifications/my       → paginated notification history for user
GET /api/notifications/admin/all→ all notifications (ADMIN)
```

---

## 12. Complete End-to-End Order Journey

```
TIME ──────────────────────────────────────────────────────────────────────────►

[T+0s]  Client: POST /api/orders
        Gateway validates JWT, injects X-User-Id

[T+0s]  Order Service:
        → Feign: bulk-check inventory (sync)
        → Feign: get product details / snapshot price (sync)
        → Feign: reserve stock (sync)
        → Save Order {status: PENDING}
        → Publish order.created
        → Return 201 to client ✓

[T+1s]  Payment Service (async, consumes order.created):
        → Process payment (simulate 90% success)
        → Save Payment record
        → Publish payment.success OR payment.failed

[T+1s]  Notification Service (async, consumes order.created):
        → Send "Order Received" email

────────────── PAYMENT SUCCESS PATH ──────────────────────────────────────────

[T+2s]  Order Service (consumes payment.success):
        → Update Order {status: CONFIRMED}
        → Publish order.confirmed

[T+2s]  Inventory Service (consumes payment.success):
        → Confirm StockReservation {status: CONFIRMED}
        → Deduct from quantityAvailable
        → Check if below lowStockThreshold → publish inventory.low.stock

[T+2s]  Notification Service (consumes payment.success):
        → Send "Payment Confirmed" email

[T+3s]  Shipping Service (consumes order.confirmed):
        → Create Shipment {TRK-XXXXXXXX, status: PROCESSING}
        → Add initial TrackingEvent "Received at Warehouse"

[T+?h]  Admin: PATCH /api/shipping/{id}/status {status: DISPATCHED}
        → Publish shipment.dispatched

[T+?h]  Order Service (consumes shipment.dispatched):
        → Update Order {status: SHIPPED}

[T+?h]  Notification Service (consumes shipment.dispatched):
        → Send "Your order is on the way!" email

[T+5d]  Admin: PATCH /api/shipping/{id}/status {status: DELIVERED}
        → Record actualDelivery date
        → Publish shipment.delivered

[T+5d]  Order Service (consumes shipment.delivered):
        → Update Order {status: DELIVERED}
        → Publish order.delivered

[T+5d]  Notification Service (consumes shipment.delivered):
        → Send "Order Delivered!" email

[T+5d+] Customer: POST /api/reviews
        → Review Service verifies order DELIVERED via Feign
        → Save Review {status: PENDING}
        → Publish review.submitted

[T+5d+] Search Service (consumes review.submitted):
        → Update averageRating + totalReviews in Elasticsearch

────────────── PAYMENT FAILED PATH ───────────────────────────────────────────

[T+2s]  Order Service (consumes payment.failed):
        → Update Order {status: CANCELLED}

[T+2s]  Inventory Service (consumes payment.failed):
        → Release StockReservation {status: RELEASED}
        → Restore quantityAvailable

[T+2s]  Notification Service (consumes payment.failed):
        → Send "Payment Failed" email
```

---

## 13. API Reference — All Services

All APIs go through the Gateway at port **8080**.  
All requests (except public) require: `Authorization: Bearer <access_token>`  
All responses use: `{ status, message, data, error, timestamp }`

### Auth Service — `/api/auth` (Port 8081)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, get tokens |
| POST | `/api/auth/refresh` | Public | Refresh access token |
| POST | `/api/auth/logout` | User | Revoke refresh token |
| POST | `/api/auth/forgot-password` | Public | Send reset email |
| POST | `/api/auth/reset-password` | Public | Reset with token |
| POST | `/api/auth/user/change-password` | User | Change password |
| POST | `/api/auth/user/change-email` | User | Change email |
| POST | `/api/auth/user/verify-email` | User | Verify email |
| GET | `/api/auth/admin/users` | Admin | List all users |
| POST | `/api/auth/admin/users/{id}/block` | Admin | Block user |
| POST | `/api/auth/admin/users/{id}/enable` | Admin | Enable user |
| DELETE | `/api/auth/admin/users/{id}` | Admin | Delete user |
| POST | `/api/auth/admin/users/{id}/roles/{role}` | Admin | Assign role |
| DELETE | `/api/auth/admin/users/{id}/roles/{role}` | Admin | Remove role |

### User Service — `/api/users` (Port 8082)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/users/me` | User | Get my profile |
| PUT | `/api/users/me` | User | Update my profile |
| GET | `/api/users/{userId}/profile` | Admin | Get any user profile |
| GET | `/api/users/me/addresses` | User | List my addresses |
| POST | `/api/users/me/addresses` | User | Add address |
| PUT | `/api/users/me/addresses/{id}` | User | Update address |
| DELETE | `/api/users/me/addresses/{id}` | User | Delete address |
| PUT | `/api/users/me/addresses/{id}/default` | User | Set default address |

### Product Service — `/api/products`, `/api/categories` (Port 8083)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/products` | Public | List active products (paginated) |
| GET | `/api/products/{id}` | Public | Get product by ID |
| POST | `/api/products` | User | Create product |
| PUT | `/api/products/{id}` | User | Update product |
| PATCH | `/api/products/{id}/status` | Admin | Change status |
| DELETE | `/api/products/{id}` | Admin | Delete product |
| GET | `/api/products/seller/{sellerId}` | Public | Products by seller |
| GET | `/api/categories` | Public | All categories |
| GET | `/api/categories/{id}` | Public | Category by ID |
| POST | `/api/categories` | Admin | Create category |
| GET | `/api/categories/{id}/products` | Public | Products in category |

### Inventory Service — `/api/inventory` (Port 8084)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/inventory` | Admin | Create inventory entry |
| GET | `/api/inventory/{productId}` | Public | Get stock level |
| PUT | `/api/inventory/{productId}/restock` | Admin | Add stock |
| GET | `/api/inventory/low-stock` | Admin | Low stock items |
| POST | `/api/inventory/bulk-check` | Internal | Check availability (used by Order Svc) |
| POST | `/api/inventory/reserve` | Internal | Reserve stock (used by Order Svc) |
| POST | `/api/inventory/confirm/{orderId}` | Internal | Confirm reservation |
| POST | `/api/inventory/release/{orderId}` | Internal | Release reservation |

### Order Service — `/api/orders` (Port 8085)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/orders` | User | Place new order |
| GET | `/api/orders/{id}` | User | Get order by ID |
| GET | `/api/orders` | User | My orders (paginated) |
| DELETE | `/api/orders/{id}/cancel` | User | Cancel order |
| GET | `/api/orders/admin/all` | Admin | All orders |
| PATCH | `/api/orders/{id}/status` | Admin | Update order status |

### Payment Service — `/api/payments` (Port 8086)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/payments/{id}` | User | Get payment by ID |
| GET | `/api/payments/order/{orderId}` | User | Payment for order |
| GET | `/api/payments/my` | User | My payment history |
| GET | `/api/payments/admin/all` | Admin | All payments |
| POST | `/api/payments/{id}/refund` | Admin | Issue refund |

### Shipping Service — `/api/shipping` (Port 8089)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/shipping/order/{orderId}` | User | Shipment for order |
| GET | `/api/shipping/{id}` | User | Shipment by ID |
| GET | `/api/shipping/track/{trackingNumber}` | Public | Track by number |
| PATCH | `/api/shipping/{id}/status` | Admin | Update status |

### Review Service — `/api/reviews` (Port 8087)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/reviews` | User | Submit review |
| GET | `/api/reviews/product/{productId}` | Public | Product reviews |
| GET | `/api/reviews/product/{productId}/summary` | Public | Rating summary |
| GET | `/api/reviews/{id}` | Public | Review by ID |
| PUT | `/api/reviews/{id}` | User | Update my review |
| DELETE | `/api/reviews/{id}` | User | Delete my review |
| POST | `/api/reviews/{id}/helpful` | User | Mark as helpful |
| GET | `/api/reviews/my` | User | My reviews |
| PATCH | `/api/reviews/{id}/status` | Admin | Approve/Reject |

### Search Service — `/api/search` (Port 8088)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/search/products?q=laptop&page=0&size=10` | Public | Full-text search |
| GET | `/api/search/products/category/{categoryId}` | Public | Browse by category |

### Notification Service — `/api/notifications` (Port 8090)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/notifications/my` | User | My notification history |
| GET | `/api/notifications/admin/all` | Admin | All notifications |

---

## 14. RabbitMQ Exchange & Queue Map

```
EXCHANGE          TYPE    PUBLISHER          QUEUE                    CONSUMER
────────────────────────────────────────────────────────────────────────────────
auth.events       Topic   Auth Service       user.profile.created.q   User Svc
                                             user.profile.disabled.q  User Svc
                                             notification.auth.q      Notif Svc

product.events    Topic   Product Svc        search.product.events.q  Search Svc
                                             (no other consumers)

order.events      Topic   Order Svc          payment.order.created.q  Payment Svc
                                             shipping.order.confirmed.q Shipping Svc
                                             review.order.delivered.q   Review Svc *
                                             inventory.order.cancelled.q Inventory Svc
                                             notification.order.q      Notif Svc
                                             order.payment.events.q  ← payment fan-in
                                             order.shipping.events.q ← shipping fan-in

payment.events    Topic   Payment Svc        inventory.payment.events.q Inventory Svc
                                             notification.payment.q    Notif Svc
                                             order.payment.events.q    Order Svc

shipping.events   Topic   Shipping Svc       notification.shipping.q   Notif Svc
                                             order.shipping.events.q   Order Svc

inventory.events  Topic   Inventory Svc      notification.inventory.q  Notif Svc

review.events     Topic   Review Svc         search.review.events.q    Search Svc
                                             notification.review.q *   Notif Svc
```

**Routing Key Conventions:**
```
auth.*          → user.created, user.disabled
product.*       → product.created, product.updated, product.deleted, product.status.changed
order.*         → order.created, order.confirmed, order.cancelled, order.delivered
payment.*       → payment.success, payment.failed, payment.refunded
shipment.*      → shipment.dispatched, shipment.delivered
inventory.*     → inventory.low.stock
review.*        → review.submitted
```

---

*Generated for portfolio documentation — E-Commerce Microservices (Spring Boot 3.5 + Spring Cloud)*
