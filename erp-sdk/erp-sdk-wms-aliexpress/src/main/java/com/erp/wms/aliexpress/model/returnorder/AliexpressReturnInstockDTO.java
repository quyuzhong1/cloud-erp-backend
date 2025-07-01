package com.erp.wms.aliexpress.model.returnorder;

import cn.hutool.core.annotation.Alias;
import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
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


        @Alias("erpCustomKey")
        private String erpCustomKey;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ReturnOrder {

        @Alias("logistics_name")
        private String logisticsName;
        @Alias("return_reason")
        private String returnReason;
        @Alias("sender_info")
        private SenderInfoDTO senderInfo;
        @Alias("warehouse_code")
        private String warehouseCode;
        @Alias("owner_code")
        private String ownerCode;
        @Alias("remark")
        private String remark;
        @Alias("logistics_code")
        private String logisticsCode;
        @Alias("pre_delivery_order_code")
        private String preDeliveryOrderCode;
        @Alias("order_flag")
        private String orderFlag;
        @Alias("express_code")
        private String expressCode;
        @Alias("pre_delivery_order_id")
        private String preDeliveryOrderId;
        @Alias("return_order_code")
        private String returnOrderCode;
        @Alias("order_type")
        private String orderType;

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class SenderInfoDTO {
            @Alias("area")
            private String area;
            @Alias("country_code")
            private String countryCode;
            @Alias("town")
            private String town;
            @Alias("province")
            private String province;
            @Alias("city")
            private String city;
            @Alias("detail_address")
            private String detailAddress;
            @Alias("mobile")
            private String mobile;
            @Alias("name")
            private String name;
            @Alias("company")
            private String company;
            @Alias("tel")
            private String tel;
            @Alias("zip_code")
            private String zipCode;
            @Alias("email")
            private String email;
        }
    }
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OrderLines {

        @Alias("item_code")
        private String itemCode;
        @Alias("plan_qty")
        private Integer planQty;
        @Alias("item_id")
        private String itemId;
        @Alias("source_order_code")
        private String sourceOrderCode;
        @Alias("owner_code")
        private String ownerCode;
        @Alias("item_name")
        private String itemName;
        @Alias("batch_code")
        private String batchCode;
        @Alias("product_date")
        private String productDate;
        @Alias("sub_source_order_code")
        private String subSourceOrderCode;
        @Alias("expire_date")
        private String expireDate;
        @Alias("order_line_no")
        private String orderLineNo;
        @Alias("produce_code")
        private String produceCode;
        @Alias("extend_props")
        private ExtendPropsDTO extendProps;
        @Alias("inventory_type")
        private String inventoryType;

        @NoArgsConstructor
        @Data
        public static class ExtendPropsDTO {
            @Alias("k1")
            private String k1;
        }
    }
}