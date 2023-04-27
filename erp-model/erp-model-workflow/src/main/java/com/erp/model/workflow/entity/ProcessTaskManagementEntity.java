package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("process_task_management")
public class ProcessTaskManagementEntity extends BaseEntity<ProcessTaskManagementEntity> {

    /**
     * 流程实例ID
     */
    @TableField("process_instance_id")
    private String processInstanceId;

    /**
     * 当前节点ID
     */
    @TableField("current_node_id")
    private String currentNodeId;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 当前审批人ID
     */
    @TableField("current_approver_id")
    private String currentApproverId;

    /**
     * 当前节点开始时间
     */
    @TableField("current_task_start_time")
    private LocalDateTime currentTaskStartTime;

    /**
     * 任务状态
     */
    @TableField("task_status")
    private ApproveStatusEnum taskStatus;

    /**
     * 超时预警状态
     */
    @TableField("timeout_warn_status")
    private Integer timeoutWarnStatus;

    /**
     * 超时预警时间
     */
    @TableField("timeout_warn_interval")
    private Integer timeoutWarnInterval;

    /**
     * 超时处理时间
     */
    @TableField("timeout_deal_interval")
    private Integer timeoutDealInterval;

    /**
     * 超时处理方式
     */
    @TableField("timeout_deal_type")
    private String timeoutDealType;


    public static final String PROCESS_INSTANCE_ID = "process_instance_id";

    public static final String CURRENT_NODE_ID = "current_node_id";

    public static final String TASK_ID = "task_id";

    public static final String CURRENT_APPROVER_ID = "current_approver_id";

    public static final String CURRENT_TASK_START_TIME = "current_task_start_time";

    public static final String TASK_STATUS = "task_status";

    public static final String TIMEOUT_WARN_STATUS = "timeout_warn_status";

    public static final String TIMEOUT_WARN_INTERVAL = "timeout_warn_interval";

    public static final String TIMEOUT_DEAL_INTERVAL = "timeout_deal_interval";

    public static final String TIMEOUT_DEAL_TYPE = "timeout_deal_type";


    public ProcessTaskManagementEntity(String processInstanceId, String activityId, String taskId, LocalDateTime startTime, ApproveStatusEnum approveStatus) {
        this.processInstanceId = processInstanceId;
        this.currentNodeId = activityId;
        this.taskId = taskId;
        this.currentTaskStartTime = startTime;
        this.taskStatus = approveStatus;

    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
