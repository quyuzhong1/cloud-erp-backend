package com.erp.model.mrp.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


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
    private LocalDate date;
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

    /**
     * 销量类型：default=默认，dynamic=动态、fixed=固定
     */
    @TableField("type")
    private String type;
    /**
     * 销量默认类型：dynamic=动态、fixed=固定
     */
    @TableField("default_type")
    private String defaultType;
    /**
     * 固定值
     */
    @TableField("fixed_value")
    private Integer fixedValue;

    @TableField(value = "percent_json", jdbcType = JdbcType.OTHER)
    private JSONObject percentJson;

    /**
     * 百分比对象
     */
    @TableField(exist = false)
    private CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO;


    public static final String CALC_SALES_INFO_DIM_ID = "calc_sales_info_dim_id";

    public static final String DATE = "date";

    public static final String QTY = "qty";

    public static final String MONTH = "month";

    @Override
    public Serializable pkVal() {
        return null;
    }

}