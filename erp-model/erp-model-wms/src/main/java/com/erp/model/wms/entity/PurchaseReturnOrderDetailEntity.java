package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 采购退货单明细
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_return_order_detail")
public class PurchaseReturnOrderDetailEntity extends BaseEntity<PurchaseReturnOrderDetailEntity> {

    /**
     * 退货单主表id
     */
    @TableField("main_id")
    private String mainId;

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
     * 实退数量
     */
    @TableField("reality_return_qty")
    private Integer realityReturnQty;

    /**
     * 补货数量
     */
    @TableField("replenish_qty")
    private Integer replenishQty;

    /**
     * 扣款数量
     */
    @TableField("deduct_amount_qty")
    private Integer deductAmountQty;

    /**
     * 退货单价
     */
    @TableField("return_price")
    private BigDecimal returnPrice;

    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 单据来源详情表id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 采购订单详情表id
     */
    @TableField("purchase_order_detail_id")
    private String purchaseOrderDetailId;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
