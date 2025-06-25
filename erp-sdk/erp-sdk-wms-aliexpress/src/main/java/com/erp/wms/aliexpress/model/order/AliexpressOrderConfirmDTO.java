package com.erp.wms.aliexpress.model.order;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AliexpressOrderConfirmDTO {

    @Alias("orderLines")
    private List<OrderLinesDTO> orderLines;
    @Alias("deliveryOrder")
    private DeliveryOrderDTO deliveryOrder;
    @Alias("packages")
    private List<PackagesDTO> packages;

    @NoArgsConstructor
    @Data
    public static class DeliveryOrderDTO {
        @Alias("orderType")
        private String orderType;
        @Alias("outBizCode")
        private String outBizCode;
        @Alias("orderConfirmTime")
        private String orderConfirmTime;
        @Alias("deliveryOrderCode")
        private String deliveryOrderCode;
        @Alias("confirmType")
        private String confirmType;
        @Alias("warehouseCode")
        private String warehouseCode;
        @Alias("deliveryOrderId")
        private String deliveryOrderId;
        @Alias("status")
        private String status;

        @Alias("orderLines")
        private List<OrderLinesDTO> orderLines;

        @Alias("packages")
        private List<PackagesDTO> packages;
    }

    @NoArgsConstructor
    @Data
    public static class OrderLinesDTO {
        @Alias("itemId")
        private String itemId;
        @Alias("batchs")
        private List<BatchsDTO> batchs;
        @Alias("orderLineNo")
        private String orderLineNo;
        @Alias("itemName")
        private String itemName;
        @Alias("planQty")
        private String planQty;
        @Alias("ownerCode")
        private String ownerCode;
        @Alias("itemCode")
        private String itemCode;
        @Alias("actualQty")
        private String actualQty;

        @NoArgsConstructor
        @Data
        public static class BatchsDTO {
            @Alias("inventoryType")
            private String inventoryType;
            @Alias("batchCode")
            private String batchCode;
            @Alias("actualQty")
            private String actualQty;
        }
    }

    @NoArgsConstructor
    @Data
    public static class PackagesDTO {
        @Alias("logisticsName")
        private String logisticsName;
        @Alias("volume")
        private String volume;
        @Alias("expressCode")
        private String expressCode;
        @Alias("length")
        private String length;
        @Alias("width")
        private String width;
        @Alias("logisticsCode")
        private String logisticsCode;
        @Alias("weight")
        private String weight;
        @Alias("items")
        private List<ItemsDTO> items;
        @Alias("height")
        private String height;

        @NoArgsConstructor
        @Data
        public static class ItemsDTO {
            @Alias("itemId")
            private String itemId;
            @Alias("quantity")
            private String quantity;
            @Alias("itemCode")
            private String itemCode;
        }
    }
}