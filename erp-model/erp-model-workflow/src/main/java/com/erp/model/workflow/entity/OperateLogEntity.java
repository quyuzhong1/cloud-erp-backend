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
 * 操作日志表
 * </p>
 *
 * @author will
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("operate_log")
public class OperateLogEntity extends BaseEntity<OperateLogEntity> {

    /**
    * 类型
    */
    @TableField("module_type")
    private String moduleType;
    /**
    * 字段名称
    */
    @TableField("field_name")
    private String fieldName;
    /**
    * 业务id
    */
    @TableField("business_id")
    private String businessId;
    /**
    * 旧值
    */
    @TableField("old_value")
    private String oldValue;
    /**
    * 新值
    */
    @TableField("new_value")
    private String newValue;
    /**
    * 内容
    */
    @TableField("content")
    private String content;
    /**
    * 操作项
    */
    @TableField("operation")
    private String operation;
    /**
    * 父级id
    */
    @TableField("pid")
    private String pid;


    public static final String MODULE_TYPE = "module_type";

    public static final String FIELD_NAME = "field_name";

    public static final String BUSINESS_ID = "business_id";

    public static final String OLD_VALUE = "old_value";

    public static final String NEW_VALUE = "new_value";

    public static final String CONTENT = "content";

    public static final String OPERATION = "operation";

    public static final String PID = "pid";

    @Override
    public Serializable pkVal() {
        return null;
    }

}