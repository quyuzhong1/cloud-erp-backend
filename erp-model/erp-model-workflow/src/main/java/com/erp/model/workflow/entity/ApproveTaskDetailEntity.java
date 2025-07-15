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
 * 三方生成查询明细
 * </p>
 *
 * @author will
 * @since 2025-05-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("approve_task_detail")
public class ApproveTaskDetailEntity extends BaseEntity<ApproveTaskDetailEntity> {

    /**
    * 三方生成查询id
    */
    @TableField("mian_id")
    private String mianId;
    /**
    * 第三方接口字段(英文)
    */
    @TableField("third_field")
    private String thirdField;
    /**
    * 第三方类型（选项、数值等）
    */
    @TableField("third_field_type")
    private String thirdFieldType;
    /**
    * 第三方字段值
    */
    @TableField("third_field_value")
    private String thirdFieldValue;
    /**
    * 第三方字段必填
    */
    @TableField("third_field_required")
    private Boolean thirdFieldRequired;
    /**
    * 说明
    */
    @TableField("third_desc")
    private String thirdDesc;
    /**
    * 数大臣字段名称
    */
    @TableField("sys_field_name")
    private String sysFieldName;
    /**
    * 数大臣接口字段(英文)
    */
    @TableField("sys_field")
    private String sysField;
    /**
    * 数大臣类型
    */
    @TableField("sys_field_type")
    private String sysFieldType;
    /**
    * 数大臣必填
    */
    @TableField("sys_field_required")
    private Boolean sysFieldRequired;
    /**
    * 数大臣字段值
    */
    @TableField("sys_field_value")
    private String sysFieldValue;
    /**
     * 排序字段
     */
    @TableField("index")
    private Integer index;
    /**
     * 实体名称
     */
    @TableField("entity_name")
    private String entityName;
    /**
     * 实体编码
     */
    @TableField("entity_code")
    private String entityCode;

    public static final String MIAN_ID = "mian_id";

    public static final String THIRD_FIELD = "third_field";

    public static final String THIRD_FIELD_TYPE = "third_field_type";

    public static final String THIRD_FIELD_VALUE = "third_field_value";

    public static final String THIRD_FIELD_REQUIRED = "third_field_required";

    public static final String THIRD_DESC = "third_desc";

    public static final String SYS_FIELD_NAME = "sys_field_name";

    public static final String SYS_FIELD = "sys_field";

    public static final String SYS_FIELD_TYPE = "sys_field_type";

    public static final String SYS_FIELD_REQUIRED = "sys_field_required";

    public static final String SYS_FIELD_VALUE = "sys_field_value";

    public static final String INDEX = "index";

    public static final String ENTITY_NAME = "entity_name";

    public static final String ENTITY_CODE = "entity_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}