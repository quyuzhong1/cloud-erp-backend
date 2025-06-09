package com.sdk.oms.tiktok.dto.tiktok.fully;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TikTokFullyShippingProviderReq {

    @Alias("delivery_mode")
    private String deliveryMode;
    @Alias("delivery_order_codes")
    private List<String> deliveryOrderCodes;
    @Alias("warehouse_code")
    private String warehouseCode;
    @Alias("total_weight")
    private TotalWeightDTO totalWeight;
    @Alias("delivery_option")
    private String deliveryOption;
    @Alias("sender_contact_id")
    private String senderContactId;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class TotalWeightDTO {
        @Alias("value")
        private String value;
        @Alias("unit")
        private String unit;
    }
}
