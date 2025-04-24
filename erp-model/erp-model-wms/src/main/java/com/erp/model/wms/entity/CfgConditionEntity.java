package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.RuleTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 条件配置表
 * </p>
 *
 * @author liaohui
 * @since 2024-06-03
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_condition")
public class CfgConditionEntity extends BaseEntity<CfgConditionEntity> {

    /**
     * 条件字段 对应dict_rule_condition key
     */
    @TableField("condition_field")
    private String conditionField;

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
     * 条件字段名
     */
    @TableField("condition_field_name")
    private String conditionFieldName;

    /**
     * 请求方式 GET POST 等
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数 JSON 格式
     */
    @TableField("param")
    private String param;

    /**
     * 对应下拉的绑定的字段
     */
    @TableField("label")
    private String label;

    /**
     * 对应下拉的显示中文的名 的字段
     */
    @TableField("value")
    private String value;

    /**
     * 排序
     */
    @TableField("index")
    private Integer index;

    /**
     * 前端要求 用来做输入值的传入
     */
    @TableField("search_key")
    private String searchKey;

    /**
     * 前端要求用来做回显
     */
    @TableField("remote_label")
    private String remoteLabel;

    /**
     * 值类型
     */
    @TableField("value_type")
    private String valueType;

    /**
     * 条件所属规则类型 PICKING_STRATEGY（拣货规则）
     * @see RuleTypeEnum
     */
    @TableField("rule_type")
    private String ruleType;
    @Override
    public Serializable pkVal() {
        return null;
    }

}
