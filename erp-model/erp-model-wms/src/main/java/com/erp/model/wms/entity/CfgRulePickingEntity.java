package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 拣货规则表
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_rule_picking")
public class CfgRulePickingEntity extends BaseEntity<CfgRulePickingEntity> {

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
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
