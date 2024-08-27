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
import java.util.List;


/**
 * <p>
 * 销量公式（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_sales_formula")
public class CfgRuleSalesFormulaEntity extends BaseEntity<CfgRuleSalesFormulaEntity> {

    /**
    * 销量类型：default=默认，dynamic=动态、fixed=固定  枚举：CfgRuleSalesFormulaTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 销量默认类型：dynamic=动态、fixed=固定  枚举：CfgRuleSalesFormulaDefaultTypeEnum
    */
    @TableField("default_type")
    private String defaultType;
    /**
    * 排序字段
    */
    @TableField("index")
    private Integer index;
    /**
    * 优先级字段
    */
    @TableField("priority")
    private Integer priority;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 开始日期
    */
    @TableField("start_date")
    private LocalDate startDate;
    /**
    * 结束日期
    */
    @TableField("end_date")
    private LocalDate endDate;
    /**
    * 销量id(cfg_rule_sales_qty)
    */
    @TableField("sales_qty_id")
    private String salesQtyId;
    /**
    * 固定值
    */
    @TableField("fixed_value")
    private BigDecimal fixedValue;
    /**
    * 百分比json
    */
    @TableField(value = "percent_json", jdbcType = JdbcType.OTHER)
    private JSONObject percentJson;

    /**
     * 百分比对象
     */
    @TableField(exist = false)
    private CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO;

    /**
     * 时间
     */
    @TableField(exist = false)
    private List<LocalDate> dateList;

    public static final String TYPE = "type";

    public static final String DEFAULT_TYPE = "default_type";

    public static final String INDEX = "index";

    public static final String PRIORITY = "priority";

    public static final String NAME = "name";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String SALES_QTY_ID = "sales_qty_id";

    public static final String FIXED_VALUE = "fixed_value";

    public static final String PERCENT_JSON = "percent_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
