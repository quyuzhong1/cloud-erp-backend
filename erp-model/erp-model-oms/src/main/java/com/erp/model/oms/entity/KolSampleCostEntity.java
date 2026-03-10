package com.erp.model.oms.entity;

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
 * 寄样费用表
 * </p>
 *
 * @author will
 * @since 2025-12-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_sample_cost")
public class KolSampleCostEntity extends BaseEntity<KolSampleCostEntity> {

    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源编码（B2B/B2C-KOL寄样申请单号）
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 寄样类型
    */
    @TableField("type")
    private String type;
    /**
    * 达人表id
    */
    @TableField("partner_id")
    private String partnerId;
    /**
    * 达人昵称
    */
    @TableField("partner_nickname")
    private String partnerNickname;
    /**
    * 回片链接
    */
    @TableField("feedback_url")
    private String feedbackUrl;
    /**
    * 销售单号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 销售id
    */
    @TableField("so_id")
    private String soId;

    /**
     * 销售明细id
     */
    @TableField("so_detail_id")
    private String soDetailId;
    /**
    * 销售组织id
    */
    @TableField("so_org_id")
    private String soOrgId;
    /**
    * 销售组织名称
    */
    @TableField("so_org_name")
    private String soOrgName;
    /**
    * 销售出库单id
    */
    @TableField("so_outstock_id")
    private String soOutstockId;
    /**
     * 销售出库单明细id
     */
    @TableField("so_outstock_detail_id")
    private String soOutstockDetailId;
    /**
    * 销售出库单编码
    */
    @TableField("so_outstock_code")
    private String soOutstockCode;
    /**
    * 销售出库日期
    */
    @TableField("so_outstock_date")
    private LocalDate soOutstockDate;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 军区id
    */
    @TableField("partition_id")
    private String partitionId;
    /**
    * 军区名称
    */
    @TableField("partition_name")
    private String partitionName;
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
    * 实发数量
    */
    @TableField("qty")
    private Integer qty;
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
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 总费用
    */
    @TableField("total_cost")
    private BigDecimal totalCost;
    /**
    * 材料成本
    */
    @TableField("product_cost")
    private BigDecimal productCost = BigDecimal.ZERO;
    /**
    * 头程费用
    */
    @TableField("first_mile_shipping_cost")
    private BigDecimal firstMileShippingCost = BigDecimal.ZERO;
    /**
    * 清关税费
    */
    @TableField("clearance_customs_tax")
    private BigDecimal clearanceCustomsTax = BigDecimal.ZERO;
    /**
    * 运费
    */
    @TableField("shipping_cost")
    private BigDecimal shippingCost = BigDecimal.ZERO;
    /**
    * 关税
    */
    @TableField("customs_tax")
    private BigDecimal customsTax = BigDecimal.ZERO;
    /**
    * 其他费用
    */
    @TableField("other_cost")
    private BigDecimal otherCost = BigDecimal.ZERO;


    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String TYPE = "type";

    public static final String PARTNER_ID = "partner_id";

    public static final String PARTNER_NICKNAME = "partner_nickname";

    public static final String FEEDBACK_URL = "feedback_url";

    public static final String SO_CODE = "so_code";

    public static final String SO_ID = "so_id";

    public static final String SO_ORG_ID = "so_org_id";

    public static final String SO_ORG_NAME = "so_org_name";

    public static final String SO_OUTSTOCK_ID = "so_outstock_id";

    public static final String SO_OUTSTOCK_CODE = "so_outstock_code";

    public static final String SO_OUTSTOCK_TIME = "so_outstock_time";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String PARTITION_ID = "partition_id";

    public static final String PARTITION_NAME = "partition_name";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String TOTAL_COST = "total_cost";

    public static final String PRODUCT_COST = "product_cost";

    public static final String FIRST_MILE_SHIPPING_COST = "first_mile_shipping_cost";

    public static final String CLEARANCE_CUSTOMS_TAX = "clearance_customs_tax";

    public static final String SHIPPING_COST = "shipping_cost";

    public static final String CUSTOMS_TAX = "customs_tax";

    public static final String OTHER_COST = "other_cost";

    @Override
    public Serializable pkVal() {
        return null;
    }

}