package com.erp.model.workflow.entity;

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
 * fieldList 明细字段映射
 * </p>
 *
 * @author hcg
 * @since 2025-05-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_process_field_sub_map")
public class CfgProcessFieldSubMapEntity extends BaseEntity<CfgProcessFieldSubMapEntity> {

    /**
    * 所属fieldList主字段的id
    */
    @TableField("parent_id")
    private String parentId;
    /**
    * 第三方字段(英文：控件name)
    */
    @TableField("third_field")
    private String thirdField;
    /**
    * 第三方类型（选项、数值等）：控件type
    */
    @TableField("third_field_type")
    private String thirdFieldType;
    /**
    * 第三方是否必填：必填
    */
    @TableField("third_field_required")
    private Boolean thirdFieldRequired;
    /**
    * 第三方字段说明：description
    */
    @TableField("third_field_description")
    private String thirdFieldDescription;
    /**
    * 数大臣字段
    */
    @TableField("sys_field")
    private String sysField;
    /**
    * 数大臣字段类型
    */
    @TableField("sys_field_type")
    private String sysFieldType;
    /**
    * 数大臣是否必填
    */
    @TableField("sys_field_required")
    private Boolean sysFieldRequired;
    /**
    * 默认值
    */
    @TableField("default_value")
    private String defaultValue;
    /**
    * 是否唯一
    */
    @TableField("is_unique")
    private Boolean isUnique;
    /**
    * 配置类型：sysCfg:系统字段配置、thirdCfg:飞书字段配置
    */
    @TableField("cfg_type")
    private String cfgType;
    /**
    * 流程定义code
    */
    @TableField("process_defintion_id")
    private String processDefintionId;


    public static final String PARENT_ID = "parent_id";

    public static final String THIRD_FIELD = "third_field";

    public static final String THIRD_FIELD_TYPE = "third_field_type";

    public static final String THIRD_FIELD_REQUIRED = "third_field_required";

    public static final String THIRD_FIELD_DESCRIPTION = "third_field_description";

    public static final String SYS_FIELD = "sys_field";

    public static final String SYS_FIELD_TYPE = "sys_field_type";

    public static final String SYS_FIELD_REQUIRED = "sys_field_required";

    public static final String DEFAULT_VALUE = "default_value";

    public static final String IS_UNIQUE = "is_unique";

    public static final String CFG_TYPE = "cfg_type";

    public static final String PROCESS_DEFINTION_ID = "process_defintion_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}