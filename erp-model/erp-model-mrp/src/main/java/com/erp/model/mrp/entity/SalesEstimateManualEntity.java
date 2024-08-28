package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 运营销量预估
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sales_estimate_manual")
public class SalesEstimateManualEntity extends BaseEntity<SalesEstimateManualEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_id")
    private String replenishmentId;

    /**
     * 当月销量预估
     */
    @TableField("current_month_sales_qty")
    private Integer currentMonthSalesQty;

    /**
     * 当月销量剩余预估
     */
    @TableField("current_month_surplus_sales_qty")
    private Integer currentMonthSurplusSalesQty;

    /**
     * 下月销量预估
     */
    @TableField("next_month_sales")
    private String nextMonthSales;

    /**
     * 后月销量预估
     */
    @TableField("following_month_sales")
    private String followingMonthSales;


    public static final String REPLENISHMENT_ID = "replenishment_id";

    public static final String CURRENT_MONTH_SALES_QTY = "current_month_sales_qty";

    public static final String CURRENT_MONTH_SURPLUS_SALES_QTY = "current_month_surplus_sales_qty";

    public static final String NEXT_MONTH_SALES = "next_month_sales";

    public static final String FOLLOWING_MONTH_SALES = "following_month_sales";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
