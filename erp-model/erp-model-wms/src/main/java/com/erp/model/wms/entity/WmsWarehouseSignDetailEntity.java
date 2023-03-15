package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 仓库签收明细单
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("wms_warehouse_sign_detail")
public class WmsWarehouseSignDetailEntity extends BaseEntity<WmsWarehouseSignDetailEntity> {

    /**
     * 签收状态（0待签收，1签收中，2已完成）
     */
    @TableField("sign_status")
    private String signStatus;

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
    @TableField("qty")
    private Integer qty;

    /**
     * 含税单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * 采购数量
     */
    @TableField("purch_qty")
    private Integer purchQty;

    /**
     * 采购金额
     */
    @TableField("purch_amount")
    private BigDecimal purchAmount;

    /**
     * 预计交货数量
     */
    @TableField("plan_sign_qty")
    private Integer planSignQty;

    /**
     * 实际交货数量
     */
    @TableField("real_sign_qty")
    private Integer realSignQty;

    /**
     * 采购订单明细id
     */
    @TableField("purch_order_detail_id")
    private String purchOrderDetailId;


    public static final String SIGN_STATUS = "sign_status";

    public static final String CODE = "code";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String PRICE = "price";

    public static final String CURRENCY = "currency";

    public static final String PURCH_QTY = "purch_qty";

    public static final String PURCH_AMOUNT = "purch_amount";

    public static final String PLAN_SIGN_QTY = "plan_sign_qty";

    public static final String REAL_SIGN_QTY = "real_sign_qty";

    public static final String PURCH_ORDER_DETAIL_ID = "purch_order_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
