package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 速卖通发货单详情
 * </p>
 *
 * @author lrp
 * @since 2024-05-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("aliexpress_delivery_detail")
public class AliexpressDeliveryDetailEntity extends BaseEntity<AliexpressDeliveryDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 平台sku
    */
    @TableField("platform_sku")
    private String platformSku;
    /**
    * 平台发货数量
    */
    @TableField("order_line_qty")
    private Integer orderLineQty;
    /**
    * ERP的sku
    */
    @TableField("sku_no")
    private String skuNo;

    /**
     * ERP的sku id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * 实际支付金额
     */
    @TableField("pay_amount")
    private BigDecimal payAmount;

    /**
     * 实际支付币别
     */
    @TableField("pay_currency")
    private String payCurrency;

    /**
     * 折扣金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 折扣币别
     */
    @TableField("discount_currency")
    private String discountCurrency;

    /**
     * 货品id
     */
    @TableField("sc_item_id")
    private String scItemId;

    /**
     * 唯一ID
     * 速卖通=中台明细ID
     */
    @TableField("unique_id")
    private String uniqueId;

    /**
     * 平台skuId
     */
    @TableField("platform_sku_id")
    private String platformSkuId;

    /**
     * 平台产品ID
     */
    @TableField("platform_spu_no")
    private String platformSpuNo;


    public static final String MAIN_ID = "main_id";

    public static final String PLATFORM_SKU = "platform_sku";

    public static final String ORDER_LINE_QTY = "order_line_qty";

    public static final String SKU_NO = "sku_no";

    @Override
    public Serializable pkVal() {
        return null;
    }

}