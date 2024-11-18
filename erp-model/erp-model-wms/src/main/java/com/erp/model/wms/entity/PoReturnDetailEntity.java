package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 采购退货单明细
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("po_return_detail")
public class PoReturnDetailEntity extends BaseEntity<PoReturnDetailEntity> {

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
     * 是否组合品 true 是
     */
    @TableField("is_combination")
    private Boolean isCombination;

    /**
     * 实退数量
     */
    @TableField("return_qty")
    private Integer returnQty;

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
     * 币别符号
     */
    @TableField("currency_symbol")
    private String currencySymbol;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 一级供应商id
     */
    @TableField("main_supplier_id")
    private String mainSupplierId;

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

    /**
     * 金蝶明细id
     */
    @TableField("kingdee_detail_id")
    private String kingdeeDetailId;

    /**
     * 仓位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    @TableField(exist = false)
    private String approveStatus;

    @TableField(exist = false)
    private LocalDateTime approveTime;


    @TableField(exist = false)
    private String returnMode;

    @TableField(exist = false)
    private LocalDate billDate;

    @TableField(exist = false)
    private String sourceType;

    @TableField(exist = false)
    private String sourceId;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
