package com.erp.wms.aliexpress.model.inbound;

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
public class AliexpressInboundDTO {

    private AliexpressAuthDTO aliexpressAuthDTO;

    private List<OrderLines> OrderLines;

    private EntryOrder entryOrder;

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
    public static class EntryOrder {

        @Alias("logistics_name")
        private String logisticsName;
        @Alias("related_orders")
        private List<RelatedOrdersDTO> relatedOrders;
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
        @Alias("purchase_order_code")
        private String purchaseOrderCode;
        @Alias("expect_end_time")
        private String expectEndTime;
        @Alias("order_create_time")
        private String orderCreateTime;
        @Alias("expect_start_time")
        private String expectStartTime;
        @Alias("entry_order_code")
        private String entryOrderCode;
        @Alias("supplier_name")
        private String supplierName;
        @Alias("order_type")
        private String orderType;
        @Alias("supplier_code")
        private String supplierCode;
        @Alias("receiver_info")
        private ReceiverInfoDTO receiverInfo;

        @NoArgsConstructor
        @Data
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

        @NoArgsConstructor
        @Data
        public static class ReceiverInfoDTO {
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

        @NoArgsConstructor
        @Data
        public static class RelatedOrdersDTO {
            @Alias("order_code")
            private String orderCode;
            @Alias("order_type")
            private String orderType;
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
        @Alias("owner_code")
        private String ownerCode;
        @Alias("item_name")
        private String itemName;
        @Alias("batch_code")
        private String batchCode;
        @Alias("retail_price")
        private String retailPrice;
        @Alias("product_date")
        private String productDate;
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