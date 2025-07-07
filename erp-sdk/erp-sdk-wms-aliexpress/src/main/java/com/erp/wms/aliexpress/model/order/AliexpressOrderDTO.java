package com.erp.wms.aliexpress.model.order;

import cn.hutool.core.annotation.Alias;
import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AliexpressOrderDTO {

    private AliexpressAuthDTO aliexpressAuthDTO;

    private List<OrderLines> OrderLines;

    private DeliveryOrder deliveryOrder;

    private ExtendProps extendProps;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ExtendProps {
        @Alias("laneCode")
        private String laneCode;
        @Alias("pickUpResCode")
        private String pickUpResCode;
        @Alias("merchantType")
        private String merchantType;
        @Alias("printInfo")
        private String printInfo;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class DeliveryOrder {

        @Alias("warehouse_code")
        private String warehouseCode;
        @Alias("freight")
        private String freight;
        @Alias("owner_code")
        private String ownerCode;
        @Alias("oaid_order_source_code")
        private String oaidOrderSourceCode;
        @Alias("remark")
        private String remark;
        @Alias("express_code")
        private String expressCode;
        @Alias("delivery_order_code")
        private String deliveryOrderCode;
        @Alias("shop_nick")
        private String shopNick;
        @Alias("pay_no")
        private String payNo;
        @Alias("item_amount")
        private String itemAmount;
        @Alias("order_type")
        private String orderType;
        @Alias("source_platform_code")
        private String sourcePlatformCode;
        @Alias("buyer_message")
        private String buyerMessage;
        @Alias("create_time")
        private String createTime;
        @Alias("sender_info")
        private SenderInfoDTO senderInfo;
        @Alias("service_fee")
        private String serviceFee;
        @Alias("seller_message")
        private String sellerMessage;
        @Alias("logistics_code")
        private String logisticsCode;
        @Alias("pre_delivery_order_code")
        private String preDeliveryOrderCode;
        @Alias("pay_time")
        private String payTime;
        @Alias("pre_delivery_order_id")
        private String preDeliveryOrderId;
        @Alias("total_amount")
        private String totalAmount;
        @Alias("logistics_area_code")
        private String logisticsAreaCode;
        @Alias("ar_amount")
        private String arAmount;
        @Alias("is_urgency")
        private String isUrgency;
        @Alias("receiver_info")
        private ReceiverInfoDTO receiverInfo;

        @NoArgsConstructor
        @Data
        public static class SenderInfoDTO {
            @Alias("area")
            private String area;
            @Alias("id_number")
            private String idNumber;
            @Alias("town")
            private String town;
            @Alias("city")
            private String city;
            @Alias("detail_address")
            private String detailAddress;
            @Alias("mobile")
            private String mobile;
            @Alias("zip_code")
            private String zipCode;
            @Alias("country_code")
            private String countryCode;
            @Alias("province")
            private String province;
            @Alias("name")
            private String name;
            @Alias("company")
            private String company;
            @Alias("tel")
            private String tel;
            @Alias("email")
            private String email;
            @Alias("oaid")
            private String oaid;
        }

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class ReceiverInfoDTO {
            @Alias("area")
            private String area;
            @Alias("id_number")
            private String idNumber;
            @Alias("town")
            private String town;
            @Alias("city")
            private String city;
            @Alias("detail_address")
            private String detailAddress;
            @Alias("mobile")
            private String mobile;
            @Alias("zip_code")
            private String zipCode;
            @Alias("country_code")
            private String countryCode;
            @Alias("province")
            private String province;
            @Alias("name")
            private String name;
            @Alias("id_type")
            private String idType;
            @Alias("company")
            private String company;
            @Alias("tel")
            private String tel;
            @Alias("email")
            private String email;
            @Alias("oaid")
            private String oaid;
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
        @Alias("actual_price")
        private String actualPrice;
        @Alias("item_id")
        private Integer itemId;
        @Alias("source_order_code")
        private String sourceOrderCode;
        @Alias("discount_amount")
        private String discountAmount;
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