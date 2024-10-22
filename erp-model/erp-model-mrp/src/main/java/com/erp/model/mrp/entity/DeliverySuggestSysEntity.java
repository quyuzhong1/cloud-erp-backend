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
 * 建议发货变更表
 * </p>
 *
 * @author will
 * @since 2024-10-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("delivery_suggest_sys")
public class DeliverySuggestSysEntity extends BaseEntity<DeliverySuggestSysEntity> {

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
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;


    public static final String SUGGEST_DELIVERY_QTY = "suggest_delivery_qty";

    public static final String SUGGEST_DELIVERY_DATE = "suggest_delivery_date";

    public static final String LOGISTICS_METHOD = "logistics_method";

    public static final String LOGISTICS_DAYS = "logistics_days";

    public static final String ESTIMATE_SALES_DATE = "estimate_sales_date";

    public static final String LOGISTICS_COST = "logistics_cost";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}