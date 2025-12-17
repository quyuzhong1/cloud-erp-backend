package com.erp.model.srm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2025-09-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("payable_detail")
public class PayableDetailEntity extends BaseEntity<PayableDetailEntity> {

    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 含税单价
    */
    @TableField("tax_included_price")
    private BigDecimal taxIncludedPrice;
    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 折扣率
    */
    @TableField("discount_rate")
    private BigDecimal discountRate;
    /**
    * 预付金额
    */
    @TableField("prepay_amount")
    private BigDecimal prepayAmount;
    /**
    * 价税合计（折扣后）
    */
    @TableField("discount_tax_amount")
    private BigDecimal discountTaxAmount;
    /**
    * 价税合计
    */
    @TableField("tax_included_total")
    private BigDecimal taxIncludedTotal;
    /**
    * 币别
    */
    @TableField("currency")
    private String currency;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 采购订单明细id
    */
    @TableField("po_detail_id")
    private String poDetailId;
    /**
    * 采购订单id
    */
    @TableField("po_id")
    private String poId;

    /**
     * 采购订单编码
     */
    @TableField("po_code")
    private String poCode;
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 业务单id,入库单id/退货单id
    */
    @TableField("business_id")
    private String businessId;
    /**
    * 业务单编码，入库单编码/退货单编码
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 业务单据类型,入库单/退货单
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 业务单明细id
    */
    @TableField("business_detail_id")
    private String businessDetailId;
    /**
    * 三方系统明细id
    */
    @TableField("third_payable_detail_id")
    private String thirdPayableDetailId;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String TAX_INCLUDED_PRICE = "tax_included_price";

    public static final String TAX_RATE = "tax_rate";

    public static final String DISCOUNT_RATE = "discount_rate";

    public static final String PREPAY_AMOUNT = "prepay_amount";

    public static final String DISCOUNT_TAX_AMOUNT = "discount_tax_amount";

    public static final String TAX_INCLUDED_TOTAL = "tax_included_total";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String PO_DETAIL_ID = "po_detail_id";

    public static final String PO_ID = "po_id";

    public static final String MAIN_ID = "main_id";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_CODE = "business_code";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String BUSINESS_DETAIL_ID = "business_detail_id";

    public static final String THIRD_PAYABLE_DETAIL_ID = "third_payable_detail_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}