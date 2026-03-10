package com.sdk.wangdian.sdk.api.virtualWarehouse.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 虚拟仓分货单推送
 */
@Getter
@Setter
public class VwPushHandelDetailPushDTO {
    /**
     * 虚拟仓编号
     */
    @SerializedName("virtual_warehouse_no")
    private String virtual_warehouse_no;
    /**
     * 单据类型:1:锁定分配,2:释放出库,3:虚拟仓间调拨,4:采购入库
     * 默认值为1
     */
    @SerializedName("order_type")
    private int order_type;
    /**
     * 目标虚拟仓编号:order_type=3时必传
     */
    @SerializedName("to_virtual_warehouse_no")
    private String to_virtual_warehouse_no;
    /**
     * 是否预审核:仅在order_type=3时生效
     * <p>
     * 1：预设审核，   0：不审核
     */
    @SerializedName("is_pre_check")
    private String isPreCheck;
    /**
     * 库存不足则不审核:默认0
     * <p>
     * 1：审核，  0：不审核
     */
    @SerializedName("insufficient_stock_not_pre_check")
    private String insufficientStockNotPreCheck;
    /**
     * 审核时间: 仅在order_type=3时生效, 格式: yyyy-MM-dd HH:mm, 时间要大于当前服务器时间2分钟以上
     */
    @SerializedName("pre_time")
    private String pre_time;
    /**
     * 备注 默认为空
     */
    @SerializedName("remark")
    private String remark;

    /**
     * 是否审核单据: 1：审核，   0：不审核
     * <p>
     * 默认0不审核
     */
    @SerializedName("is_check")
    private Integer is_check;

    @SerializedName("detailList")
    private List<DetailList> detailList;

    /**
     * （非同步字段）业务同步类型：allocation分货单
     */
    @SerializedName("bizType")
    private String bizType;
    /**
     * （非同步字段）业务id
     */
    @SerializedName("sourceId")
    private String sourceId;

    /**
     * 同步id
     */
    private String dmpSyncTaskId;


    @Getter
    @Setter
    public static class DetailList {
        /**
         * 商家编码
         */
        @SerializedName("spec_no")
        private String spec_no;
        /**
         * 实体仓编码
         */
        @SerializedName("warehouse_no")
        private String warehouse_no;
        /**
         * 入库数量，默认为1
         */
        @SerializedName("num")
        private BigDecimal num;
        /**
         * 成本价：成本，默认为0
         */
        @SerializedName("price")
        private BigDecimal price;
        /**
         * 采购在途数量，默认为0
         */
        @SerializedName("purchase_num")
        private BigDecimal purchaseNum;
        /**
         * 自定义数量，默认为0
         */
        @SerializedName("factory_num")
        private BigDecimal factoryNum;
    }
}
