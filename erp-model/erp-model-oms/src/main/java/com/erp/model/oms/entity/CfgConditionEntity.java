package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


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
@TableName("cfg_condition")
public class CfgConditionEntity extends BaseEntity<CfgConditionEntity> {

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

    /**
     * 请求方式 GET POST 等
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     *请求参数json 根式
     */
    @TableField("param")
    private String param;

    /**
     *对应下拉的绑定的字段
     */
    @TableField("label")
    private String label;

    /**
     *对应下拉的显示中文的名 的字段
     */
    @TableField("value")
    private String value;

    @TableField("index")
    private Integer index;

    /**
     * 值的类型
     */
    @TableField("value_type")
    private String valueType;
    /**
     *规则备注（removeSpace 去空格）多个以逗号分割
     * CfgConditionRuleEnum
     *
     */
    @TableField("remark")
    private String remark;


    public static final String CONDITION_FIELD = "condition_field";

    public static final String LOGIC = "logic";

    public static final String CONTROLS = "controls";

    public static final String API_URL = "api_url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}