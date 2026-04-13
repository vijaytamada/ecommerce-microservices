package com.company.notification_service.messaging.consumer;

import com.company.notification_service.config.RabbitMQConfig;
import com.company.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final NotificationService notificationService;

    /* ---- Auth Events ---- */
    @RabbitListener(queues = RabbitMQConfig.NOTIF_AUTH_QUEUE)
    public void onAuthEvent(Map<String, Object> event) {
        String type  = str(event, "eventType");
        String email = str(event, "email");
        UUID userId  = toUuid(event, "userId");
        notificationService.sendAndSave(userId, email, type,
                "Security Alert - E-Commerce",
                "A security event occurred on your account: " + type + ". If this wasn't you, please contact support.");
    }

    /* ---- Order Events ---- */
    @RabbitListener(queues = RabbitMQConfig.NOTIF_ORDER_QUEUE)
    public void onOrderEvent(Map<String, Object> event) {
        String type  = str(event, "eventType");
        String email = str(event, "userEmail");
        UUID userId  = toUuid(event, "userId");
        String orderId = str(event, "orderId");

        String subject = switch (type) {
            case "ORDER_CONFIRMED"  -> "Order Confirmed - E-Commerce";
            case "ORDER_CANCELLED"  -> "Order Cancelled - E-Commerce";
            case "ORDER_SHIPPED"    -> "Your Order is on the Way! - E-Commerce";
            case "ORDER_DELIVERED"  -> "Order Delivered - E-Commerce";
            default -> "Order Update - E-Commerce";
        };

        String body = "Your order #" + orderId + " status: " + type.replace("ORDER_", "").replace("_", " ");
        notificationService.sendAndSave(userId, email, type, subject, body);
    }

    /* ---- Payment Events ---- */
    @RabbitListener(queues = RabbitMQConfig.NOTIF_PAYMENT_QUEUE)
    public void onPaymentEvent(Map<String, Object> event) {
        String type  = str(event, "eventType");
        String email = str(event, "userEmail");
        UUID userId  = toUuid(event, "userId");
        String amount = event.getOrDefault("amount", "N/A").toString();
        String txn    = str(event, "transactionId");

        String subject = switch (type) {
            case "PAYMENT_SUCCESS"  -> "Payment Successful - E-Commerce";
            case "PAYMENT_FAILED"   -> "Payment Failed - E-Commerce";
            case "PAYMENT_REFUNDED" -> "Refund Processed - E-Commerce";
            default -> "Payment Update - E-Commerce";
        };
        String body = type + " | Amount: " + amount + (txn != null ? " | TXN: " + txn : "");
        notificationService.sendAndSave(userId, email, type, subject, body);
    }

    /* ---- Shipping Events ---- */
    @RabbitListener(queues = RabbitMQConfig.NOTIF_SHIPPING_QUEUE)
    public void onShippingEvent(Map<String, Object> event) {
        String type   = str(event, "eventType");
        UUID userId   = toUuid(event, "userId");
        String tracking = str(event, "trackingNumber");

        String subject = type.equals("SHIPMENT_DISPATCHED")
                ? "Your Package is Dispatched! - E-Commerce"
                : "Package Delivered! - E-Commerce";
        String body = type + " | Tracking: " + tracking;
        notificationService.sendAndSave(userId, null, type, subject, body);
    }

    /* ---- Inventory Events ---- */
    @RabbitListener(queues = RabbitMQConfig.NOTIF_INVENTORY_QUEUE)
    public void onInventoryEvent(Map<String, Object> event) {
        String type = str(event, "eventType");
        String productId = str(event, "productId");
        int qty = event.containsKey("quantityAvailable") ? (int) event.get("quantityAvailable") : 0;
        log.warn("LOW STOCK ALERT: productId={}, available={}", productId, qty);
        // In a full implementation: fetch seller info and notify them
        notificationService.sendAndSave(null, null, type,
                "Low Stock Alert", "Product " + productId + " has only " + qty + " units left.");
    }

    /* ---- Helpers ---- */
    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    private UUID toUuid(Map<String, Object> map, String key) {
        try { return UUID.fromString(str(map, key)); } catch (Exception e) { return null; }
    }
}
