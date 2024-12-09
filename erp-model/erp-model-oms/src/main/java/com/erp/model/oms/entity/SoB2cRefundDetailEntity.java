package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 退款订单明细
 * </p>
 *
 * @author lrp
 * @since 2024-09-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_refund_detail")
public class SoB2cRefundDetailEntity extends BaseEntity<SoB2cRefundDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 平台sku
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * 销售数量
    */
    @TableField("sale_qty")
    private Integer saleQty;
    /**
    * 退款数量
    */
    @TableField("refund_qty")
    private Integer refundQty;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String SALE_QTY = "sale_qty";

    public static final String REFUND_QTY = "refund_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}