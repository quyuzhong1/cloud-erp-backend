package com.sdk.oms.temu.dto;

import com.alibaba.fastjson.annotation.JSONField;
import jnr.ffi.annotations.In;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class TemuOrderDTO {

    @JSONField(name = "result")
    private ResultDTO result;
    @JSONField(name = "success")
    private Boolean success;
    @JSONField(name = "errorCode")
    private Integer errorCode;
    @JSONField(name = "serverTime")
    private Long serverTime;
    @JSONField(name = "errorMsg")
    private String errorMsg;

    @NoArgsConstructor
    @Data
    public static class ResultDTO {
        @JSONField(name = "totalItemNum")
        private Integer totalItemNum;
        @JSONField(name = "pageItems")
        private List<PageItemsDTO> pageItems;


        @NoArgsConstructor
        @Data
        public static class PageItemsDTO {
            @JSONField(name = "parentOrderMap")
            private ParentOrderMapDTO parentOrderMap;
            @JSONField(name = "orderList")
            private List<OrderListDTO> orderList;

            private List<String> warehouseIdList;
            private Boolean hasWarehouse;
            private String trackNo;

            @NoArgsConstructor
            @Data
            public static class ParentOrderMapDTO {
                @JSONField(name = "parentOrderLabel")
                private List<ParentOrderLabelDTO> parentOrderLabel;
                @JSONField(name = "parentShippingTime")
                private Integer parentShippingTime;
                @JSONField(name = "updateTime")
                private Integer updateTime;
                @JSONField(name = "latestDeliveryTime")
                private Integer latestDeliveryTime;
                @JSONField(name = "fulfillmentWarning")
                private List<?> fulfillmentWarning;
                @JSONField(name = "parentOrderTime")
                private Integer parentOrderTime;
                @JSONField(name = "regionId")
                private Integer regionId;
                @JSONField(name = "parentOrderPendingFinishTime")
                private Integer parentOrderPendingFinishTime;
                @JSONField(name = "parentOrderSn")
                private String parentOrderSn;
                @JSONField(name = "siteId")
                private Integer siteId;
                @JSONField(name = "expectShipLatestTime")
                private Integer expectShipLatestTime;
                @JSONField(name = "hasShippingFee")
                private Object hasShippingFee;
                @JSONField(name = "parentOrderStatus")
                private Integer parentOrderStatus;

                @NoArgsConstructor
                @Data
                public static class ParentOrderLabelDTO {
                    @JSONField(name = "name")
                    private String name;
                    @JSONField(name = "value")
                    private Integer value;
                }
            }

            @NoArgsConstructor
            @Data
            public static class OrderListDTO {

                @JSONField(name = "warehouseProviderBrandName")
                private String warehouseProviderBrandName;
                @JSONField(name = "warehouseName")
                private String warehouseName;
                @JSONField(name = "warehouseProviderCode")
                private String warehouseProviderCode;
                @JSONField(name = "warehouseCode")
                private String warehouseCode;

                @JSONField(name = "canceledQuantityBeforeShipment")
                private Integer canceledQuantityBeforeShipment;
                @JSONField(name = "quantity")
                private Integer quantity;
                @JSONField(name = "orderSn")
                private String orderSn;
                @JSONField(name = "goodsId")
                private Long goodsId;
                @JSONField(name = "orderLabel")
                private List<OrderLabelDTO> orderLabel;
                @JSONField(name = "inventoryDeductionWarehouseId")
                private String inventoryDeductionWarehouseId;
                @JSONField(name = "orderStatus")
                private Integer orderStatus;
                @JSONField(name = "fulfillmentType")
                private String fulfillmentType;
                @JSONField(name = "isCancelledDuringPending")
                private Boolean isCancelledDuringPending;
                @JSONField(name = "fulfillmentWarning")
                private List<?> fulfillmentWarning;
                @JSONField(name = "spec")
                private String spec;
                @JSONField(name = "orderPaymentType")
                private String orderPaymentType;
                @JSONField(name = "originalOrderQuantity")
                private Integer originalOrderQuantity;
                @JSONField(name = "thumbUrl")
                private String thumbUrl;
                @JSONField(name = "inventoryDeductionWarehouseName")
                private String inventoryDeductionWarehouseName;
                @JSONField(name = "goodsName")
                private String goodsName;
                @JSONField(name = "productList")
                private List<ProductListDTO> productList;
                @JSONField(name = "skuId")
                private Long skuId;

                @NoArgsConstructor
                @Data
                public static class OrderLabelDTO {
                    @JSONField(name = "name")
                    private String name;
                    @JSONField(name = "value")
                    private Integer value;
                }

                @NoArgsConstructor
                @Data
                public static class ProductListDTO {
                    @JSONField(name = "productSkuId")
                    private Long productSkuId;
                    @JSONField(name = "soldFactor")
                    private Integer soldFactor;
                    @JSONField(name = "extCode")
                    private String extCode;
                    @JSONField(name = "productId")
                    private Long productId;
                }
            }
        }
    }
}
