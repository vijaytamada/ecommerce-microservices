package com.company.shipping_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ithink")
public class IThinkProperties {

    /** iThink Logistics API v3 base URL */
    private String baseUrl = "https://my.ithinklogistics.com";

    /** Access token from iThink portal */
    private String accessToken = "";

    /** Secret key from iThink portal */
    private String secretKey = "";

    /** Pickup address ID configured in iThink portal */
    private String pickupAddressId = "";

    /** Return / warehouse address ID configured in iThink portal */
    private String returnAddressId = "";

    /** Your warehouse pincode — used for rate calculations */
    private String warehousePincode = "";

    /**
     * When true (default): no real API calls — generates TRK-XXXXXXXXXX tracking numbers.
     * When false: calls iThink Logistics API v3.
     */
    private boolean simulate = true;
}
