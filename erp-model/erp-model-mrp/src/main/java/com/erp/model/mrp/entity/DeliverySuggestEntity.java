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
import java.time.LocalDateTime;


/**
 * <p>
 * 发货计划
 * </p>
 *
 * @author will
 * @since 2024-08-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("delivery_suggest")
public class DeliverySuggestEntity extends BaseEntity<DeliverySuggestEntity> {

    /**
    * 编码
    */
    @TableField("code")
    private String code;
    /**
    * 数据类型（auto系统，manual人工）
    */
    @TableField("data_type")
    private String dataType;
    /**
    * 建议发货量
    */
    @TableField("suggest_delivery_qty")
    private Integer suggestDeliveryQty;

    /**
    * 建议发货日期
    */
    @TableField("suggest_delivery_date")
    private LocalDate suggestDeliveryDate;
    /**
    * 物流方式,LogisticsMethodEnum枚举
    */
    @TableField("logistics_method")
    private String logisticsMethod;
    /**
    * 物流时效（天）
    */
    @TableField("logistics_days")
    private Integer logisticsDays;
    /**
    * 预计可售日期
    */
    @TableField("estimate_sales_date")
    private LocalDate estimateSalesDate;
    /**
    * 物流成本
    */
    @TableField("logistics_cost")
    private BigDecimal logisticsCost;
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
     * 国家
     */
    @TableField("country")
    private String country;

    /**
     * skuid
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 计划发货量（计划修正值）
     */
    @TableField("plan_delivery_qty")
    private Integer planDeliveryQty;

    /**
     * 实际发货量（运营确认值）
     */
    @TableField("actual_delivery_qty")
    private Integer actualDeliveryQty;

    /**
     * 发货备货量
     */
    @TableField("delivery_stock_up_qty")
    private Integer deliveryStockUpQty;

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

    public static final String CREATE_TYPE = "create_type";

    public static final String SUGGEST_DELIVERY_QTY = "suggest_delivery_qty";

    public static final String SUGGEST_DELIVERY_DATE = "suggest_delivery_date";

    public static final String LOGISTICS_METHOD = "logistics_method";

    public static final String LOGISTICS_DAYS = "logistics_days";

    public static final String ESTIMATE_SALES_DATE = "estimate_sales_date";

    public static final String LOGISTICS_COST = "logistics_cost";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}