package com.sdk.oms.tiktok.dto.tiktok.fully;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TikTokFullyShippingReq {

    @Alias("delivery_mode")
    private String deliveryMode;
    @Alias("delivery_order_codes")
    private List<String> deliveryOrderCodes;

    @Alias("total_weight")
    private TotalWeightDTO totalWeight;
    @Alias("shipping_box_quantity")
    private Integer shippingBoxQuantity;
    @Alias("sender_contact_id")
    private String senderContactId;
    @Alias("logistics")
    private LogisticsDTO logistics;
    @Alias("reserve")
    private ReserveInfoDTO reserveInfo;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class TotalWeightDTO {
        @Alias("value")
        private String value;
        @Alias("unit")
        private String unit;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class LogisticsDTO {
        @Alias("delivery_option")
        private String deliveryOption;
        @Alias("shipping_provider_code")
        private String shippingProviderCode;
        @Alias("shipping_provider_name")
        private String shippingProviderName;
    }

    @NoArgsConstructor
    @Data
    public static class ReserveInfoDTO {
        @Alias("predicted_ship_time")
        private Integer predictedShipTime;
        @Alias("predicted_pickup_time")
        private Integer predictedPickupTime;
        @Alias("predicted_pickup_ge")
        private Integer predictedPickupGe;
        @Alias("predicted_pickup_lt")
        private Integer predictedPickupLt;
        @Alias("predicted_arrived_time")
        private Integer predictedArrivedTime;
    }
}
