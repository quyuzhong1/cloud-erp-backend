package com.sdk.wangdian.sdk.api.wms.stockin.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundStockinRequest {

    @SerializedName("start_time")
    private String startTime;
    @SerializedName("end_time")
    private String endTime;
    @SerializedName("status")
    private String status;
    @SerializedName("warehouse_no")
    private String warehouseNo;
    @SerializedName("stockin_no")
    private String stockinNo;
    @SerializedName("refund_no")
    private String refundNo;
    @SerializedName("shop_nos")
    private String shopNos;
    @SerializedName("time_type")
    private String timeType;
    @SerializedName("is_slave")
    private Boolean isSlave = false;
    @SerializedName("need_sn")
    private Boolean needSn;

}
