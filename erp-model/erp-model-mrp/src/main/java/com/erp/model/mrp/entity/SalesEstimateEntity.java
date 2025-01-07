package com.erp.model.mrp.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

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
@EqualsAndHashCode(callSuper = true)
public class SalesEstimateEntity extends BaseEntity<SalesEstimateEntity> {

    private static final long serialVersionUID = -3257441012406520824L;
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
    private BigDecimal salesQty;

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

    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String DATE = "date";

    public static final String SALES_QTY = "sales_qty";

    public static final String MONTH = "month";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
