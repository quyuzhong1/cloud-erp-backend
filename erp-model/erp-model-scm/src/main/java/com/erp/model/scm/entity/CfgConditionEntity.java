package com.erp.model.scm.entity;

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
 * 
 * </p>
 *
 * @author jack
 * @since 2025-06-16
*/
@Data
@Accessors(chain = true)
@TableName("cfg_condition")
public class CfgConditionEntity extends BaseEntity<CfgConditionEntity> {

    /**
    * 条件字段
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
    * 条件所属规则类型
    */
    @TableField("rule_type")
    private String ruleType;


    public static final String CONDITION_FIELD = "condition_field";

    public static final String LOGIC = "logic";

    public static final String CONTROLS = "controls";

    public static final String API_URL = "api_url";

    public static final String CONDITION_FIELD_NAME = "condition_field_name";

    public static final String REQUEST_METHOD = "request_method";

    public static final String PARAM = "param";

    public static final String LABEL = "label";

    public static final String VALUE = "value";

    public static final String INDEX = "index";

    public static final String SEARCH_KEY = "search_key";

    public static final String REMOTE_LABEL = "remote_label";

    public static final String VALUE_TYPE = "value_type";

    public static final String RULE_TYPE = "rule_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}