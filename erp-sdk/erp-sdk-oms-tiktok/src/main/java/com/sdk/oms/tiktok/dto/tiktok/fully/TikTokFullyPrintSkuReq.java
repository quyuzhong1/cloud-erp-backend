package com.sdk.oms.tiktok.dto.tiktok.fully;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TikTokFullyPrintSkuReq {

    @Alias("stockup_order_code")
    private String stockupOrderCode;
    @Alias("size")
    private SizeDTO size;
    @Alias("platform_sku_items")
    private List<PlatformSkuItemsDTO> platformSkuItems;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class SizeDTO {
        @Alias("width")
        private String width;
        @Alias("height")
        private String height;
        @Alias("unit")
        private String unit;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class PlatformSkuItemsDTO {
        @Alias("code")
        private String code;
        @Alias("quantity")
        private Integer quantity;
    }
}
