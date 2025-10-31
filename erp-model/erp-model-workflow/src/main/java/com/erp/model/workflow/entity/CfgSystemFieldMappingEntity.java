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
 * 远程查询配置
 * </p>
 *
 * @author will
 * @since 2025-10-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_system_field_mapping")
public class CfgSystemFieldMappingEntity extends BaseEntity<CfgSystemFieldMappingEntity> {

    /**
    * cfg_query_option表id
    */
    @TableField("cfg_query_option_id")
    private String cfgQueryOptionId;
    /**
    * 接口显示字段
    */
    @TableField("source_display_field")
    private String sourceDisplayField;
    /**
    * 接口来源字段
    */
    @TableField("source_field")
    private String sourceField;
    /**
    * 业务保存字段
    */
    @TableField("business_field")
    private String businessField;
    /**
    * 远程查询路径（serviceImpl）
    */
    @TableField("feign_path")
    private String feignPath;
    /**
    * 远程查询方法
    */
    @TableField("feign_method")
    private String feignMethod;
    /**
    * 远程查询参数
    */
    @TableField("feign_param")
    private String feignParam;
    /**
    * 目标字段
    */
    @TableField("target_field")
    private String targetField;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
     * 系统所属
     */
    @TableField(exist = false)
    private String sysParentId;

    public static final String CFG_QUERY_OPTION_ID = "cfg_query_option_id";

    public static final String SOURCE_DISPLAY_FIELD = "source_display_field";

    public static final String SOURCE_FIELD = "source_field";

    public static final String BUSINESS_FIELD = "business_field";

    public static final String FEIGN_PATH = "feign_path";

    public static final String FEIGN_METHOD = "feign_method";

    public static final String FEIGN_PARAM = "feign_param";

    public static final String TARGET_FIELD = "target_field";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}