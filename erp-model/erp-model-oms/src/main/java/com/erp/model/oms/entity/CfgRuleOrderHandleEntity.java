package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;


/**
 * <p>
 * 订单处理规则表
 * </p>
 *
 * @author will
 * @since 2024-05-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "cfg_rule_order_handle", autoResultMap = true)
public class CfgRuleOrderHandleEntity extends BaseEntity<CfgRuleOrderHandleEntity> {

    /**
    * 规则名称
    */
    @TableField("name")
    private String name;
    /**
    * 禁用状态 false 未禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 规则描述
    */
    @TableField("remark")
    private String remark;
    /**
    * 优先级
    */
    @TableField("priority")
    private Integer priority;

    /**
     * 规则内容
     */
    @TableField(value = "rule_content", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> ruleContent;

    public static final String NAME = "name";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String PRIORITY = "priority";

    public static final String IS_PUSH_CITY = "is_push_city";

    public static final String IS_PUSH_PROVINCE = "is_push_province";

    @Override
    public Serializable pkVal() {
        return null;
    }

}