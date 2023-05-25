package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.workflow.enums.CcStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Cloud
 * @since 2023-05-17
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("process_task_cc")
public class ProcessTaskCcEntity extends BaseEntity<ProcessTaskCcEntity> {

    /**
     * 任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 抄送人id
     */
    @TableField("cc_user_id")
    private String ccUserId;

    /**
     * 抄送人名称
     */
    @TableField("cc_user_name")
    private String ccUserName;

    /**
     * 抄送任务状态 
     */
    @TableField("status")
    private CcStatusEnum status;

    /**
     * 任务管理ID
     */
    private String taskManagementId;


    public static final String TASK_ID = "task_id";

    public static final String REMARK = "remark";

    public static final String CC_USER_ID = "cc_user_id";

    public static final String CC_USER_NAME = "cc_user_name";

    public static final String STATUS = "status";

    public static final String TASK_MANAGEMENT_ID = "task_management_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
