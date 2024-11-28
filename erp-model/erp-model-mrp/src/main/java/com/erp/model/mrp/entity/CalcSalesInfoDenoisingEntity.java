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
 * 试算销量去噪
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("calc_sales_info_denoising")
public class CalcSalesInfoDenoisingEntity extends BaseEntity<CalcSalesInfoDenoisingEntity> {

    /**
    * 试算id
    */
    @TableField("calc_sales_info_dim_id")
    private String calcSalesInfoDimId;
    /**
    * 日期
    */
    @TableField("date")
    private LocalDate date;
    /**
    * 销量
    */
    @TableField("qty")
    private BigDecimal qty;
    /**
    * 去噪类型，percentage百分比去噪，fixedValue固定值去噪，completely完全去噪
    */
    @TableField("denoising_type")
    private String denoisingType;

    /**
     * 有效值（去噪后的）
     */
    @TableField("effective_value")
    private Integer effectiveValue;


    public static final String CALC_SALES_INFO_DIM_ID = "calc_sales_info_dim_id";

    public static final String DATE = "date";

    public static final String QTY = "qty";

    public static final String DENOISING_TYPE = "denoising_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}