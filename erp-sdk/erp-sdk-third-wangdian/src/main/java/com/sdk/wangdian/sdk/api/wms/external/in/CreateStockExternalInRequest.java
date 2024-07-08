package com.sdk.wangdian.sdk.api.wms.external.in;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateStockExternalInRequest {

    private Order order;

    @SerializedName("order_details")
    private List<OrderDetail> orderDetails;

    @SerializedName("is_check")
    private Boolean isCheck;

    @Getter
    @Setter
    public static class Order {
        private String warehouseNo;
        private String remark;
        @SerializedName("order_no")
        private String orderNo;
        @SerializedName("src_order_type")
        private String srcOrderType;
        @SerializedName("src_order_no")
        private String srcOrderNo;
        private String reason;
    }

    @Getter
    @Setter
    public static class OrderDetail {
        @SerializedName("spec_no")
        private String specNo;
        private BigDecimal num;
        private String remark;

    }
}
