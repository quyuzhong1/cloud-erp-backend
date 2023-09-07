package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 条件配置表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cf_condition")
public class CfConditionEntity extends BaseEntity<CfConditionEntity> {

    /**
    * 条件字段 对应dict_rule_condition key
    */
    @TableField("condition_field")
    private String conditionField;


    /**
     * 条件字段名
     */
    @TableField("condition_field_name")
    private String conditionFieldName;
    /**
    * 逻辑关系 对应 dict_rule_condition key 多个逗号分割
    */
    @TableField("logic")
    private String logic;
    /**
    * 空间 如时间戳 输入框之类
    */
    @TableField("controls")
    private String controls;
    /**
    * 对应api url
    */
    @TableField("api_url")
    private String apiUrl;


    public static final String CONDITION_FIELD = "condition_field";

    public static final String LOGIC = "logic";

    public static final String CONTROLS = "controls";

    public static final String API_URL = "api_url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}