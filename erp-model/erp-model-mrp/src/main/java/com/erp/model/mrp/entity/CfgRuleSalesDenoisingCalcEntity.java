package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 试算销量去噪信息
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_sales_denoising_calc")
public class CfgRuleSalesDenoisingCalcEntity extends BaseEntity<CfgRuleSalesDenoisingCalcEntity> {

    /**
    * 序号
    */
    @TableField("index")
    private Integer index;
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
    * 去噪类型，percentage百分比去噪：fixedValue=固定值去噪，completely=完全去噪  枚举：CfgRuleSalesDenoisingCalcDenoisingTypeEnum
    */
    @TableField("denoising_type")
    private String denoisingType;
    /**
    * 有效值（去噪后的）
    */
    @TableField("effective_value")
    private Integer effectiveValue;
    /**
    * 试算配置id
    */
    @TableField("cfg_rule_calc_id")
    private String cfgRuleCalcId;


    public static final String INDEX = "index";

    public static final String NAME = "name";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String DENOISING_TYPE = "denoising_type";

    public static final String EFFECTIVE_VALUE = "effective_value";

    public static final String CFG_RULE_CALC_ID = "cfg_rule_calc_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
