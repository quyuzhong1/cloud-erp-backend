package com.sdk.oms.tiktok.dto.tiktok.fully;

import cn.hutool.core.annotation.Alias;
import jnr.ffi.annotations.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TikTokFullyDeliveryReq {

    @Alias("stockup_order_code")
    private String stockupOrderCode;
    @Alias("package_quantity")
    private Integer packageQuantity;
    @Alias("packages")
    private List<PackagesDTO> packages;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class PackagesDTO {
        @Alias("items")
        private List<ItemsDTO> items;

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        public static class ItemsDTO {
            @Alias("platform_sku_code")
            private String platformSkuCode;
            @Alias("quantity")
            private Integer quantity;
        }
    }
}
