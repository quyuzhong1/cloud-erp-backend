package com.sdk.wangdian.sdk.api.goods.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class VwAllocationHandelDetailPushDTO {
    /**
     * 虚拟仓编号
     */
    @SerializedName("virtual_warehouse_no")
    private String virtualWarehouseNo;
    /**
     * 单据类型:1:锁定分配,2:释放出库,3:虚拟仓间调拨,4:采购入库
     * 默认值为1
     */
    @SerializedName("order_type")
    private String orderType;
    /**
     * 目标虚拟仓编号:order_type=3时必传
     */
    @SerializedName("to_virtual_warehouse_no")
    private String toVirtualWarehouseNo;
    /**
     * 是否预审核:仅在order_type=3时生效
     *
     * 1：预设审核，   0：不审核
     */
    @SerializedName("is_pre_check")
    private String isPreCheck;
    /**
     * 库存不足则不审核:仅在order_type=3时生效，默认0
     *
     * 1：审核，  0：不审核
     */
    @SerializedName("insufficient_stock_not_pre_check")
    private String insufficientStockNotPreCheck;
    /**
     * 审核时间: 仅在order_type=3时生效, 格式: yyyy-MM-dd HH:mm, 时间要大于当前服务器时间2分钟以上
     */
    @SerializedName("pre_time")
    private String preTime;
    /**
     * 备注 默认为空
     */
    @SerializedName("remark")
    private boolean remark;
    /**
     *是否审核单据: 1：审核，   0：不审核
     *
     * 默认0不审核
     */
    @SerializedName("is_check")
    private Integer isCheck;

}
