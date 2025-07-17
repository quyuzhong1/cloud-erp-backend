package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 流程设置执行条件
 * </p>
 *
 * @author hcg
 * @since 2025-05-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_process_rule")
public class CfgProcessRuleEntity extends BaseEntity<CfgProcessRuleEntity> {

    /**
    * 流程类型：erpProgress=ERP流程,fsProgress=飞书流程  枚举：CfgProcessRuleTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 流程配置id
    */
    @TableField("cfg_process_id")
    private String cfgProcessId;
    /**
    * 流程定义id
    */
    @TableField("process_definition_id")
    private String processDefinitionId;
    /**
    * 版本
    */
    @TableField("process_definition_version")
    private Integer processDefinitionVersion;
    /**
    * 是否默认
    */
    @TableField("is_default")
    private Boolean isDefault;
    /**
    * 启用状态
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String TYPE = "type";

    public static final String CFG_PROCESS_ID = "cfg_process_id";

    public static final String PROCESS_DEFINITION_ID = "process_definition_id";

    public static final String PROCESS_DEFINITION_VERSION = "process_definition_version";

    public static final String IS_DEFAULT = "is_default";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
