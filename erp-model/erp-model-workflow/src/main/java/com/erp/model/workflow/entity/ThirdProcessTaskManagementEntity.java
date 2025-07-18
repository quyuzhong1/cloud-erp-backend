package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2025-05-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_process_task_management")
public class ThirdProcessTaskManagementEntity extends BaseEntity<ThirdProcessTaskManagementEntity> {

    /**
    * 节点id
    */
    @TableField("node_id")
    private String nodeId;
    /**
    * 任务id
    */
    @TableField("task_id")
    private String taskId;
    /**
    * third_process_management_id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 节点名称
    */
    @TableField("node_name")
    private String nodeName;
    /**
    * 任务状态
    */
    @TableField("task_status")
    private String taskStatus;
    /**
    * 系统用户id
    */
    @TableField("sys_user_id")
    private String sysUserId;
    /**
    * 三方平台审批人的 user_id
    */
    @TableField("third_user_id")
    private String thirdUserId;
    /**
    * 任务开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 任务结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;


    public static final String NODE_ID = "node_id";

    public static final String TASK_ID = "task_id";

    public static final String MAIN_ID = "main_id";

    public static final String NODE_NAME = "node_name";

    public static final String TASK_STATUS = "task_status";

    public static final String SYS_USER_ID = "sys_user_id";

    public static final String THIRD_USER_ID = "third_user_id";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}