package com.company.shipping_service.client;

import com.company.shipping_service.config.IThinkProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * iThink Logistics API v3 client.
 *
 * Every request is a POST with body: { "data": { access_token, secret_key, ...payload } }
 * Docs: https://docs.ithinklogistics.com/
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IThinkClient {

    private final IThinkProperties props;
    private final RestTemplate restTemplate;

    // ── Core POST helper ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, Map<String, Object> payload) {
        Map<String, Object> data = new HashMap<>();
        data.put("access_token", props.getAccessToken());
        data.put("secret_key",   props.getSecretKey());
        data.putAll(payload);

        Map<String, Object> body = Map.of("data", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = props.getBaseUrl() + path;

        log.debug("[iThink] POST {} payload keys={}", path, payload.keySet());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map<String, Object> result = response.getBody();
        log.debug("[iThink] Response status_code={}", result != null ? result.get("status_code") : "null");
        return result != null ? result : Map.of();
    }

    // ── Pincode serviceability ────────────────────────────────────────────────

    /** Check if a pincode is serviceable. Returns raw iThink response. */
    public Map<String, Object> checkPincode(String pincode) {
        return post("/api_v3/pincode/check.json", Map.of("pincode", String.valueOf(pincode)));
    }

    // ── Rate calculator ───────────────────────────────────────────────────────

    public Map<String, Object> getRate(String fromPincode, String toPincode,
                                       double lengthCm, double widthCm, double heightCm,
                                       double weightKg, String paymentMethod, double productMrp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("from_pincode",            fromPincode);
        payload.put("to_pincode",              toPincode);
        payload.put("shipping_length_cms",     String.valueOf(lengthCm));
        payload.put("shipping_width_cms",      String.valueOf(widthCm));
        payload.put("shipping_height_cms",     String.valueOf(heightCm));
        payload.put("shipping_weight_kg",      String.valueOf(weightKg));
        payload.put("order_type",              "forward");
        payload.put("payment_method",          paymentMethod.toLowerCase()); // "cod" | "prepaid"
        payload.put("product_mrp",             String.valueOf(productMrp));
        return post("/api_v3/rate/check.json", payload);
    }

    // ── Create shipment ───────────────────────────────────────────────────────

    /**
     * Creates a forward or reverse shipment.
     * @param shipmentPayload  flat map matching iThink's required fields
     * @param orderType        "forward" (default) | "reverse"
     */
    public Map<String, Object> createOrder(Map<String, Object> shipmentPayload, String orderType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("shipments",         List.of(shipmentPayload));
        payload.put("pickup_address_id", props.getPickupAddressId());
        payload.put("logistics",         "");
        payload.put("s_type",            "");
        payload.put("order_type",        orderType != null ? orderType : "forward");
        return post("/api_v3/order/add.json", payload);
    }

    // ── Track by AWB ─────────────────────────────────────────────────────────

    public Map<String, Object> trackOrder(String awb) {
        return post("/api_v3/order/track.json", Map.of("awb_number_list", awb));
    }

    // ── Cancel shipment ───────────────────────────────────────────────────────

    public Map<String, Object> cancelOrder(String awb) {
        return post("/api_v3/order/cancel.json", Map.of("awb_numbers", awb));
    }

    // ── Shipping label PDF ────────────────────────────────────────────────────

    /** Returns map with "file_name" key containing the label PDF URL. */
    public Map<String, Object> getLabel(String awb) {
        return post("/api_v3/shipping/label.json", Map.of("awb_numbers", awb));
    }

    // ── Manifest PDF ──────────────────────────────────────────────────────────

    /** Returns map with "file_name" key containing the manifest PDF URL. */
    public Map<String, Object> getManifest(String awb) {
        return post("/api_v3/shipping/manifest.json", Map.of("awb_numbers", awb));
    }
}
