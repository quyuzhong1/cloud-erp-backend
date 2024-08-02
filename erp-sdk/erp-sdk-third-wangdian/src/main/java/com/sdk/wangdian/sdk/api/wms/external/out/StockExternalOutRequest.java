package com.sdk.wangdian.sdk.api.wms.external.out;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockExternalOutRequest {

    /**
     * 入库单号
     */
    @SerializedName("outer_out_no")
    private String outerOutNo;
    /**
     * 仓库编号
     */
    @SerializedName("warehouse_no")
    private String warehouseNo;
    /**
     * 开始时间
     */
    @SerializedName("start_time")
    private String startTime;
    /**
     * 结束时间
     */
    @SerializedName("end_time")
    private String endTime;
    /**
     * 时间类型
     */
    @SerializedName("time_type")
    private String timeType;
    /**
     * 物流单号
     */
    @SerializedName("logistics_no")
    private String logisticsNo;
    /**
     * 入库类型
     */
    @SerializedName("src_order_type")
    private String srcOrderType;
    /**
     * 状态
     */
    @SerializedName("status")
    private String status;
}
