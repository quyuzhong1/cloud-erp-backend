package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 出库配置规则
 * </p>
 *
 * @author lrp
 * @since 2024-06-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "cfg_rule_out", autoResultMap = true)
public class CfgRuleOutEntity extends BaseEntity<CfgRuleOutEntity> {
    private static final long serialVersionUID = 2405172041950251807L;
    /**
    * 配置规则类型
    */
    @TableField("type")
    private String type;
    /**
    * 规则内容
    */
    @TableField(value = "rule_content", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> ruleContent;
    @Override
    public Serializable pkVal() {
        return null;
    }

}