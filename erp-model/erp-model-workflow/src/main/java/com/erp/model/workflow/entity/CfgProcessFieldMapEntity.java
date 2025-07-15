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
 * 流程设置字段配置
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_process_field_map")
public class CfgProcessFieldMapEntity extends BaseEntity<CfgProcessFieldMapEntity> {

    /**
    * 第三方字段(英文：控件name)
    */
    @TableField("third_field")
    private String thirdField;
    /**
    * 第三方类型（选项、数值等）：控件type,CfgQueryOptionFieldTypeEnum枚举
    */
    @TableField("third_field_type")
    private String thirdFieldType;
    /**
    * 第三方是否必填：必填
    */
    @TableField("third_field_required")
    private Boolean thirdFieldRequired;
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
    * 配置id
    */
    @TableField("cfg_id")
    private String cfgId;
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
     * 第三方字段id，唯一标识
     */
    @TableField("third_field_id")
    private String thirdFieldId;
    /**
     * 第三方字段是否是明细控件
     */
    @TableField("is_detail_field")
    private Boolean isDetailField;
    /**
     * 第三方字段所属明细控件id
     */
    @TableField("third_parent_id")
    private String thirdParentId;
    /**
     * 排序字段
     */
    @TableField("index")
    private int index;
    /**
     * 系统明细字段所属父字段id（query_option中的parentId）
     */
    @TableField("sys_parent_id")
    private String sysParentId;
    /**
     * 组别类型（0正常级别，1集合父项，2集合子项）
     */
    @TableField("group_type")
    private String groupType;


    public static final String THIRD_FIELD = "third_field";

    public static final String THIRD_FIELD_TYPE = "third_field_type";

    public static final String THIRD_FIELD_REQUIRED = "third_field_required";

    public static final String THIRD_FIELD_DESCRIPTION = "third_field_description";

    public static final String SYS_FIELD = "sys_field";

    public static final String SYS_FIELD_TYPE = "sys_field_type";

    public static final String SYS_FIELD_REQUIRED = "sys_field_required";

    public static final String CFG_ID = "cfg_id";

    public static final String DEFAULT_VALUE = "default_value";

    public static final String IS_UNIQUE = "is_unique";

    public static final String CFG_TYPE = "cfg_type";

    public static final String THIRD_FIELD_ID = "third_field_id";

    public static final String IS_DETAIL_FIELD = "is_detail_field";

    public static final String PARENT_ID = "parent_id";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}