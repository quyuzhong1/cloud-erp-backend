package com.erp.wms.aliexpress.model.inbound;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AliexpressInboundConfirmDTO {

    @Alias("orderLines")
    private List<OrderLinesDTO> orderLines;
    @Alias("entryOrder")
    private EntryOrder entryOrder;
    @Alias("totalOrders")
    private List<TotalOrders> totalOrders;

    @NoArgsConstructor
    @Data
    public static class EntryOrder {

        @Alias("orderLines")
        private List<OrderLinesDTO> orderLines;

        @Alias("packages")
        private List<TotalOrders> packages;

        @Alias("entryOrderType")
        private String entryOrderType;
        @Alias("entryOrderCode")
        private String entryOrderCode;
        @Alias("outBizCode")
        private String outBizCode;
        @Alias("ownerCode")
        private String ownerCode;
        @Alias("operateTime")
        private String operateTime;
        @Alias("entryOrderId")
        private String entryOrderId;
        @Alias("confirmType")
        private String confirmType;
        @Alias("warehouseCode")
        private String warehouseCode;
        @Alias("status")
        private String status;
    }

    @NoArgsConstructor
    @Data
    public static class OrderLinesDTO {

        @Alias("itemId")
        private String itemId;
        @Alias("batchs")
        private List<BatchsDTO> batchs;
        @Alias("itemName")
        private String itemName;
        @Alias("inventoryType")
        private String inventoryType;
        @Alias("ownerCode")
        private String ownerCode;
        @Alias("itemCode")
        private String itemCode;
        @Alias("actualQty")
        private String actualQty;

        @NoArgsConstructor
        @Data
        public static class BatchsDTO {
            @Alias("produceCode")
            private String produceCode;
            @Alias("inventoryType")
            private String inventoryType;
            @Alias("batchCode")
            private String batchCode;
            @Alias("expireDate")
            private String expireDate;
            @Alias("productDate")
            private String productDate;
            @Alias("actualQty")
            private String actualQty;
        }
    }

    @NoArgsConstructor
    @Data
    public static class TotalOrders {

        @Alias("itemId")
        private String itemId;
        @Alias("itemName")
        private String itemName;
        @Alias("itemCode")
        private String itemCode;
        @Alias("actualQty")
        private String actualQty;
    }
}