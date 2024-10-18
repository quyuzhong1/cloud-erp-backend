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
 * 建议采购
 * </p>
 *
 * @author will
 * @since 2024-08-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("purchase_suggest")
public class PurchaseSuggestEntity extends BaseEntity<PurchaseSuggestEntity> {

    /**
    * 编码
    */
    @TableField("code")
    private String code;
    /**
    * 创建类型（auto系统，manual人工）
    */
    @TableField("create_type")
    private String createType;
    /**
    * 建议采购量
    */
    @TableField("suggest_purchase_qty")
    private Integer suggestPurchaseQty;
    /**
     * 系统建议采购量
     */
    @TableField("sys_suggest_purchase_qty")
    private Integer sysSuggestPurchaseQty;
    /**
    * 建议采购日期
    */
    @TableField("suggest_purchase_date")
    private LocalDate suggestPurchaseDate;
    /**
     * 系统建议采购日期
     */
    @TableField("sys_suggest_purchase_date")
    private LocalDate sysSuggestPurchaseDate;
    /**
    * 物流方式
    */
    @TableField("logistics_method")
    private String logisticsMethod;
    /**
     * 系统物流方式
     */
    @TableField("sys_logistics_method")
    private String sysLogisticsMethod;
    /**
    * 物流时效（天）
    */
    @TableField("logistics_days")
    private Integer logisticsDays;
    /**
     * 系统物流时效（天）
     */
    @TableField("sys_logistics_days")
    private Integer sysLogisticsDays;
    /**
    * 预计入库日期
    */
    @TableField("estimate_instock_date")
    private LocalDate estimateInstockDate;
    /**
     * 系统预计入库日期
     */
    @TableField("sys_estimate_instock_date")
    private LocalDate sysEstimateInstockDate;
    /**
    * 预计可售日期
    */
    @TableField("estimate_sales_date")
    private LocalDate estimateSalesDate;
    /**
     * 系统预计可售日期
     */
    @TableField("sys_estimate_sales_date")
    private LocalDate sysEstimateSalesDate;
    /**
    * 采购成本
    */
    @TableField("purchase_cost")
    private BigDecimal purchaseCost;
    /**
     * 系统采购成本
     */
    @TableField("sys_purchase_cost")
    private BigDecimal sysPurchaseCost;
    /**
    * 作废状态
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 作废原因
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
     * 状态
     */
    @TableField("status")
    private String status;
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

    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * 平台类型
     */
    @TableField("platform_type")
    private String platformType;

    /**
     * 平台
     */
    @TableField("platform")
    private String platform;

    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * skuid
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 计划采购量（计划修正值）
     */
    @TableField("plan_purchase_qty")
    private Integer planPurchaseQty;

    /**
     * 采购备货量
     */
    @TableField("purchase_stock_up_qty")
    private Integer purchaseStockUpQty;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    public static final String CODE = "code";

    public static final String CREATE_TYPE = "create_type";

    public static final String SUGGEST_PURCHASE_QTY = "suggest_purchase_qty";

    public static final String SUGGEST_PURCHASE_DATE = "suggest_purchase_date";

    public static final String LOGISTICS_METHOD = "logistics_method";

    public static final String LOGISTICS_DAYS = "logistics_days";

    public static final String ESTIMATE_INSTOCK_DATE = "estimate_instock_date";

    public static final String ESTIMATE_SALES_DATE = "estimate_sales_date";

    public static final String PURCHASE_COST = "purchase_cost";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}