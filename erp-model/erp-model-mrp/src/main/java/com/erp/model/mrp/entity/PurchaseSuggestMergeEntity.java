package com.erp.model.mrp.entity;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * 建议采购(合并后)
 * </p>
 *
 * @author will
 * @since 2024-10-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("purchase_suggest_merge")
public class PurchaseSuggestMergeEntity extends BaseEntity<PurchaseSuggestMergeEntity> {

    /**
    * 编码
    */
    @TableField("code")
    private String code;
    /**
    * 创建类型
    */
    @TableField("data_type")
    private String dataType;
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
    * 来源idJsonArray
    */
    @TableField(value = "source_id_json", jdbcType = JdbcType.OTHER)
    private JSONArray sourceIdJson;
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
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
     * bom版本
     */
    @TableField("bom_version")
    private String bomVersion;
    /**
    * 状态
    */
    @TableField("status")
    private String status;
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

    /**
     * 作废时间
     */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;
    /**
     * 作废人id
     */
    @TableField("invalid_user_id")
    private String invalidUserId;
    /**
     * 作废人名称
     */
    @TableField("invalid_user_name")
    private String invalidUserName;


    public static final String CODE = "code";

    public static final String DATA_TYPE = "data_type";

    public static final String SUGGEST_PURCHASE_QTY = "suggest_purchase_qty";

    public static final String SUGGEST_PURCHASE_DATE = "suggest_purchase_date";

    public static final String LOGISTICS_METHOD = "logistics_method";

    public static final String LOGISTICS_DAYS = "logistics_days";

    public static final String ESTIMATE_INSTOCK_DATE = "estimate_instock_date";

    public static final String ESTIMATE_SALES_DATE = "estimate_sales_date";

    public static final String PURCHASE_COST = "purchase_cost";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String SOURCE_ID_JSON = "source_id_json";

    public static final String SOURCE_TYPE = "source_type";

    public static final String CURRENCY = "currency";

    public static final String PLATFORM_TYPE = "platform_type";

    public static final String PLATFORM = "platform";

    public static final String SHOP_ID = "shop_id";

    public static final String SKU_ID = "sku_id";

    public static final String STATUS = "status";

    public static final String PLAN_PURCHASE_QTY = "plan_purchase_qty";

    public static final String PURCHASE_STOCK_UP_QTY = "purchase_stock_up_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}