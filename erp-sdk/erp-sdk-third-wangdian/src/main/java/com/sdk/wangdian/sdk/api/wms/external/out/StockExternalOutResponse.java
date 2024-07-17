package com.sdk.wangdian.sdk.api.wms.external.out;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StockExternalOutResponse {

    @SerializedName("total_count")
    private Integer totalCount;
    @SerializedName("order")
    private List<Order> order;

    @Getter
    @Setter
    public static class Order {
        @SerializedName("rec_id")
        private String recId;
        @SerializedName("outer_out_no")
        private String outerOutNo;
        @SerializedName("warehouse_name")
        private String warehouseName;
        @SerializedName("warehouse_no")
        private String warehouseNo;
        @SerializedName("logistics_name")
        private String logisticsName;
        @SerializedName("logistics_no")
        private String logisticsNo;
        @SerializedName("goods_count")
        private String goodsCount;
        @SerializedName("goods_type_count")
        private String goodsTypeCount;
        @SerializedName("status")
        private String status;
        @SerializedName("reason")
        private String reason;
        @SerializedName("creator_name")
        private String creatorName;
        @SerializedName("src_order_type")
        private String srcOrderType;
        @SerializedName("src_order_no")
        private String srcOrderNo;
        @SerializedName("remark")
        private String remark;
        @SerializedName("note_count")
        private String noteCount;
        @SerializedName("created")
        private String created;
        @SerializedName("modified")
        private String modified;
        @SerializedName("detail_list")
        private List<OrderDetail> detailList;
    }

    @Getter
    @Setter
    public static class OrderDetail {

        @SerializedName("rec_id")
        private String recId;
        @SerializedName("spec_no")
        private String specNo;
        @SerializedName("goods_no")
        private String goodsNo;
        @SerializedName("goods_name")
        private String goodsName;
        @SerializedName("spec_code")
        private String specCode;
        @SerializedName("spec_name")
        private String specName;
        @SerializedName("barcode")
        private String barcode;
        @SerializedName("num")
        private String num;
        @SerializedName("num2")
        private String num2;
        @SerializedName("defect")
        private Boolean defect;
        @SerializedName("remark")
        private String remark;
        @SerializedName("unit_ratio")
        private String unitRatio;
        @SerializedName("aux_unit_name")
        private String auxUnitName;
        @SerializedName("base_unit_name")
        private String baseUnitName;
        @SerializedName("batch_no")
        private String batchNo;
        @SerializedName("expire_date")
        private String expireDate;
    }
}
