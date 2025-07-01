package com.erp.wms.aliexpress.model.returnorder;

import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AliexpressReturnInstockDTO {

    private AliexpressAuthDTO aliexpressAuthDTO;

    private List<OrderLines> OrderLines;

    private ReturnOrder returnOrder;

    private ExtendProps extendProps;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ExtendProps {


        @JsonProperty("erpCustomKey")
        private String erpCustomKey;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ReturnOrder {

        @JsonProperty("logistics_name")
        private String logisticsName;
        @JsonProperty("return_reason")
        private String returnReason;
        @JsonProperty("sender_info")
        private SenderInfoDTO senderInfo;
        @JsonProperty("warehouse_code")
        private String warehouseCode;
        @JsonProperty("owner_code")
        private String ownerCode;
        @JsonProperty("remark")
        private String remark;
        @JsonProperty("logistics_code")
        private String logisticsCode;
        @JsonProperty("pre_delivery_order_code")
        private String preDeliveryOrderCode;
        @JsonProperty("order_flag")
        private String orderFlag;
        @JsonProperty("express_code")
        private String expressCode;
        @JsonProperty("pre_delivery_order_id")
        private String preDeliveryOrderId;
        @JsonProperty("return_order_code")
        private String returnOrderCode;
        @JsonProperty("order_type")
        private String orderType;

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class SenderInfoDTO {
            @JsonProperty("area")
            private String area;
            @JsonProperty("country_code")
            private String countryCode;
            @JsonProperty("town")
            private String town;
            @JsonProperty("province")
            private String province;
            @JsonProperty("city")
            private String city;
            @JsonProperty("detail_address")
            private String detailAddress;
            @JsonProperty("mobile")
            private String mobile;
            @JsonProperty("name")
            private String name;
            @JsonProperty("company")
            private String company;
            @JsonProperty("tel")
            private String tel;
            @JsonProperty("zip_code")
            private String zipCode;
            @JsonProperty("email")
            private String email;
        }
    }
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OrderLines {

        @JsonProperty("item_code")
        private String itemCode;
        @JsonProperty("plan_qty")
        private Integer planQty;
        @JsonProperty("item_id")
        private String itemId;
        @JsonProperty("source_order_code")
        private String sourceOrderCode;
        @JsonProperty("owner_code")
        private String ownerCode;
        @JsonProperty("item_name")
        private String itemName;
        @JsonProperty("batch_code")
        private String batchCode;
        @JsonProperty("product_date")
        private String productDate;
        @JsonProperty("sub_source_order_code")
        private String subSourceOrderCode;
        @JsonProperty("expire_date")
        private String expireDate;
        @JsonProperty("order_line_no")
        private String orderLineNo;
        @JsonProperty("produce_code")
        private String produceCode;
        @JsonProperty("extend_props")
        private ExtendPropsDTO extendProps;
        @JsonProperty("inventory_type")
        private String inventoryType;

        @NoArgsConstructor
        @Data
        public static class ExtendPropsDTO {
            @JsonProperty("k1")
            private String k1;
        }
    }
}