package com.erp.model.mrp.entity;

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
 * 建议采购变更
 * </p>
 *
 * @author will
 * @since 2024-10-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("purchase_suggest_sys")
public class PurchaseSuggestSysEntity extends BaseEntity<PurchaseSuggestSysEntity> {

    /**
    * 建议采购量
    */
    @TableField("suggest_purchase_qty")
    private Integer suggestPurchaseQty;
    /**
    * 建议采购日期
    */
    @TableField("suggest_purchase_date")
    private LocalDate suggestPurchaseDate;
    /**
    * 物流方式
    */
    @TableField("logistics_method")
    private String logisticsMethod;
    /**
    * 物流时效（天）
    */
    @TableField("logistics_days")
    private Integer logisticsDays;
    /**
    * 预计入库日期
    */
    @TableField("estimate_instock_date")
    private LocalDate estimateInstockDate;
    /**
    * 预计可售日期
    */
    @TableField("estimate_sales_date")
    private LocalDate estimateSalesDate;
    /**
    * 采购成本
    */
    @TableField("purchase_cost")
    private BigDecimal purchaseCost;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;


    public static final String SUGGEST_PURCHASE_QTY = "suggest_purchase_qty";

    public static final String SUGGEST_PURCHASE_DATE = "suggest_purchase_date";

    public static final String LOGISTICS_METHOD = "logistics_method";

    public static final String LOGISTICS_DAYS = "logistics_days";

    public static final String ESTIMATE_INSTOCK_DATE = "estimate_instock_date";

    public static final String ESTIMATE_SALES_DATE = "estimate_sales_date";

    public static final String PURCHASE_COST = "purchase_cost";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}