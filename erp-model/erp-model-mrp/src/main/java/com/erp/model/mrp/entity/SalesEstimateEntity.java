package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * <p>
 * 销量预估
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sales_estimate")
public class SalesEstimateEntity extends BaseEntity<SalesEstimateEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 日期
     */
    @TableField("date")
    private LocalDate date;

    /**
     * 销量
     */
    @TableField("sales_qty")
    private Integer salesQty;

    /**
     * 日均销预估
     */
    @TableField("avg_daily_sales_qty")
    private Integer avgDailySalesQty;

    /**
     * 所属月份
     */
    @TableField("month")
    private String month;

    /**
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String DATE = "date";

    public static final String SALES_QTY = "sales_qty";

    public static final String AVG_DAILY_SALES_QTY = "avg_daily_sales_qty";

    public static final String MONTH = "month";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
