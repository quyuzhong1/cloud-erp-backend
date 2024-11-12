package com.erp.model.mrp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 试算销量预估
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("calc_sales_info_estimate")
public class CalcSalesInfoEstimateEntity extends BaseEntity<CalcSalesInfoEstimateEntity> {

    /**
    * 试算id
    */
    @TableField("calc_sales_info_dim_id")
    private String calcSalesInfoDimId;
    /**
    * 日期
    */
    @TableField("date")
    private LocalDateTime date;
    /**
    * 销量
    */
    @TableField("qty")
    private BigDecimal qty;
    /**
    * 所属月份
    */
    @TableField("month")
    private String month;


    public static final String CALC_SALES_INFO_DIM_ID = "calc_sales_info_dim_id";

    public static final String DATE = "date";

    public static final String QTY = "qty";

    public static final String MONTH = "month";

    @Override
    public Serializable pkVal() {
        return null;
    }

}