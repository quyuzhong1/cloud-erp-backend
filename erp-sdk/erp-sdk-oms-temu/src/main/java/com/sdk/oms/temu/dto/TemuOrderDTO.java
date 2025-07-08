package com.sdk.oms.temu.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class TemuOrderDTO {


    @JSONField(name = "totalItemNum")
    private Long totalItemNum;
    @JSONField(name = "pageItems")
    private List<PageItemsDTO> pageItems;

    @NoArgsConstructor
    @Data
    public static class PageItemsDTO {
        @JSONField(name = "parentOrderMap")
        private ParentOrderMapDTO parentOrderMap;
        @JSONField(name = "orderList")
        private List<OrderListDTO> orderList;

        @NoArgsConstructor
        @Data
        public static class ParentOrderMapDTO {
            @JSONField(name = "parentOrderLabel")
            private List<ParentOrderLabelDTO> parentOrderLabel;
            @JSONField(name = "parentShippingTime")
            private Long parentShippingTime;
            @JSONField(name = "updateTime")
            private Long updateTime;
            @JSONField(name = "latestDeliveryTime")
            private Long latestDeliveryTime;
            @JSONField(name = "fulfillmentWarning")
            private List<?> fulfillmentWarning;
            @JSONField(name = "parentOrderTime")
            private Long parentOrderTime;
            @JSONField(name = "regionId")
            private Long regionId;
            @JSONField(name = "parentOrderSn")
            private String parentOrderSn;
            @JSONField(name = "parentOrderPendingFinishTime")
            private Long parentOrderPendingFinishTime;
            @JSONField(name = "siteId")
            private Long siteId;
            @JSONField(name = "expectShipLatestTime")
            private Long expectShipLatestTime;
            @JSONField(name = "hasShippingFee")
            private Object hasShippingFee;
            @JSONField(name = "parentOrderStatus")
            private Long parentOrderStatus;

            @NoArgsConstructor
            @Data
            public static class ParentOrderLabelDTO {
                @JSONField(name = "name")
                private String name;
                @JSONField(name = "value")
                private Long value;
            }
        }

        private Boolean hasWarehouse = false;

        private TemuLogisticShipmentDTO.ShipmentInfoDTODTO shipmentInfoDTODTO;
        private String trackNo;

        private String shopId;
        private String shopName;
        private String parentOrderSn;
        @NoArgsConstructor
        @Data
        public static class OrderListDTO {
            @JSONField(name = "canceledQuantityBeforeShipment")
            private Long canceledQuantityBeforeShipment;
            @JSONField(name = "quantity")
            private Long quantity;
            @JSONField(name = "orderSn")
            private String orderSn;
            @JSONField(name = "goodsId")
            private Long goodsId;
            @JSONField(name = "orderLabel")
            private List<OrderLabelDTO> orderLabel;
            @JSONField(name = "orderStatus")
            private Long orderStatus;
            @JSONField(name = "inventoryDeductionWarehouseId")
            private Object inventoryDeductionWarehouseId;
            @JSONField(name = "fulfillmentType")
            private String fulfillmentType;
            @JSONField(name = "isCancelledDuringPending")
            private Boolean isCancelledDuringPending;
            @JSONField(name = "spec")
            private String spec;
            @JSONField(name = "fulfillmentWarning")
            private List<?> fulfillmentWarning;
            @JSONField(name = "orderPaymentType")
            private String orderPaymentType;
            @JSONField(name = "originalOrderQuantity")
            private Long originalOrderQuantity;
            @JSONField(name = "thumbUrl")
            private String thumbUrl;
            @JSONField(name = "goodsName")
            private String goodsName;
            @JSONField(name = "inventoryDeductionWarehouseName")
            private Object inventoryDeductionWarehouseName;
            @JSONField(name = "skuId")
            private Long skuId;
            @JSONField(name = "productList")
            private List<ProductListDTO> productList;

            private String warehouseCode;

            @NoArgsConstructor
            @Data
            public static class OrderLabelDTO {
                @JSONField(name = "name")
                private String name;
                @JSONField(name = "value")
                private Long value;
            }

            @NoArgsConstructor
            @Data
            public static class ProductListDTO {
                @JSONField(name = "productSkuId")
                private Long productSkuId;
                @JSONField(name = "soldFactor")
                private Long soldFactor;
                @JSONField(name = "extCode")
                private String extCode;
                @JSONField(name = "productId")
                private Long productId;
            }
        }
    }
}
