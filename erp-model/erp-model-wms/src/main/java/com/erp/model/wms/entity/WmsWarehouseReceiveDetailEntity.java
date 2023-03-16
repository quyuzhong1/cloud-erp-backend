package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 仓库签收明细单
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("wms_warehouse_receive_detail")
public class WmsWarehouseReceiveDetailEntity extends BaseEntity<WmsWarehouseReceiveDetailEntity> {

    /**
     * 签收状态（0待签收，1签收中，2已完成）
     */
    @TableField("receive_status")
    private String receiveStatus;

    /**
     * 签收单号
     */
    @TableField("code")
    private String code;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 单箱数量
     */
    @TableField("unit_qty")
    private Integer unitQty;

    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * 采购数量
     */
    @TableField("purchase_qty")
    private Integer purchaseQty;

    /**
     * 预计交货数量
     */
    @TableField("plan_receive_qty")
    private Integer planReceiveQty;

    /**
     * 实际交货数量
     */
    @TableField("real_receive_qty")
    private Integer realReceiveQty;

    /**
     * 采购订单明细id
     */
    @TableField("purchase_order_detail_id")
    private String purchaseOrderDetailId;


    public static final String RECEIVE_STATUS = "receive_status";

    public static final String CODE = "code";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String UNIT_QTY = "unit_qty";

    public static final String CURRENCY = "currency";

    public static final String PURCHASE_QTY = "purchase_qty";

    public static final String PLAN_RECEIVE_QTY = "plan_receive_qty";

    public static final String REAL_RECEIVE_QTY = "real_receive_qty";

    public static final String PURCHASE_ORDER_DETAIL_ID = "purchase_order_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
