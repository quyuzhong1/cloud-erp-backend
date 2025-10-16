package com.erp.model.srm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * <p>
 * 采购对账单明细
 * </p>
 *
 * @author will
 * @since 2024-01-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("po_reconciliation_detail")
public class PoReconciliationDetailEntity extends BaseEntity<PoReconciliationDetailEntity> {

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 对账单Id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源订单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 采购订单编码
    */
    @TableField("po_code")
    private String poCode;
    /**
    * 采购订单id
    */
    @TableField("po_id")
    private String poId;

    /**
     * 采购订单详情id
     */
    @TableField("po_detail_id")
    private String poDetailId;
    /**
    * 日期
    */
    @TableField("date")
    private LocalDate date;
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
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 含税单价
    */
    @TableField("tax_price")
    private BigDecimal taxPrice;
    /**
    * 价税合计
    */
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    /**
    * 结算组织id
    */
    @TableField("settle_org_id")
    private String settleOrgId;
    /**
    * 结算组织名称
    */
    @TableField("settle_org_Name")
    private String settleOrgName;
    /**
    * 结算方式
    */
    @TableField("settle_dict")
    private String settleDict;
    /**
    * 付款条件
    */
    @TableField("payment_condition")
    private String paymentCondition;
    /**
    * 业务状态
    */
    @TableField("business_status")
    private String businessStatus;
    /**
    * 供方备注
    */
    @TableField("supplier_remark")
    private String supplierRemark;
    /**
    * 采方备注
    */
    @TableField("purchase_remark")
    private String purchaseRemark;
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
     * 退货来源类型
     */
    @TableField("return_source_type")
    private String returnSourceType;

    /**
     * 对账状态，poReconciliationDetailStatus字典
     */
    @TableField("status")
    private String status;

    /**
     * 送货单id
     */
    @TableField("delivery_id")
    private String deliveryId;

    /**
     * 送货单明细id
     */
    @TableField("delivery_detail_id")
    private String deliveryDetailId;

    /**
     * 送货单号
     */
    @TableField("delivery_code")
    private String deliveryCode;

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
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 金蝶详情Id
     */
    @TableField("kingdee_detail_id")
    private String KingdeeDetailId;

    /**
     * 应付单类型
     */
    @TableField(exist = false)
    private String payableType;

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String MAIN_ID = "main_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String PO_CODE = "po_code";

    public static final String PO_ID = "po_id";

    public static final String DATE = "date";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String TAX_RATE = "tax_rate";

    public static final String TAX_PRICE = "tax_price";

    public static final String TAX_AMOUNT = "tax_amount";

    public static final String SETTLE_ORG_ID = "settle_org_id";

    public static final String SETTLE_ORG_NAME = "settle_org_Name";

    public static final String SETTLE_DICT = "settle_dict";

    public static final String PAYMENT_CONDITION = "payment_condition";

    public static final String BUSINESS_STATUS = "business_status";

    public static final String SUPPLIER_REMARK = "supplier_remark";

    public static final String PURCHASE_REMARK = "purchase_remark";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String IS_ADD_ACCOUNT = "is_add_account";

    @Override
    public Serializable pkVal() {
        return null;
    }

}