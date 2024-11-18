package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.tms.dto.ExtendJsonDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 
 * </p>
 *
 * @author Will
 * @since 2023-11-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("shipping_template_other_cost")
public class ShippingTemplateOtherCostEntity extends BaseEntity<ShippingTemplateOtherCostEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 费用编码
    */
    @TableField("dict_code")
    private String dictCode;
    /**
     * 费用名称
     */
    @TableField("dict_name")
    private String dictName;
    /**
    * 计算方式
    */
    @TableField("calculation_method")
    private String calculationMethod;
    /**
    * 计算方式单位
    */
    @TableField("calculation_unit")
    private String calculationUnit;
    /**
    * 费用设置值
    */
    @TableField(value = "cost_setting_value" )
    private BigDecimal costSettingValue;
    /**
    * 数值设置json
    */
    @TableField(value ="extend_json")
    private String extendJson;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
     * 辅助字段：计算方式选项
     */
    @TableField(exist = false)
    private List<String> settingList;

    /**
     * 数值设置json
     */
    @TableField(exist = false)
    private ExtendJsonDTO.CommonDTO extendJsonDto;

    /**
     * 数值设置json(用于日志)
     */
    @TableField(exist = false)
    private String extendJsonLog;


    public static final String MAIN_ID = "main_id";

    public static final String DICT_CODE = "dict_code";

    public static final String DICT_NAME = "dict_name";

    public static final String CALCULATION_METHOD = "calculation_method";

    public static final String CALCULATION_UNIT = "calculation_unit";

    public static final String COST_SETTING_VALUE = "cost_setting_value";

    public static final String EXTEND_JSON = "extend_json";

    public static final String FIELD_REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}