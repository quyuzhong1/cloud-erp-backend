package com.erp.model.workflow.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.erp.model.workflow.dto.CamundaDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
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
    @TableField("current_approve_id")
    private String currentApproveId;

    /**
     * 当前节点开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

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
     * 超时时间
     */
    @TableField("timeout_interval")
    private Integer timeoutInterval;

    /**
     * 超时处理方式
     */
    @TableField("timeout_handle_type")
    private String timeoutHandleType;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("remark")
    private String remark;


    public static final String PROCESS_INSTANCE_ID = "process_instance_id";

    public static final String CURRENT_NODE_ID = "current_node_id";

    public static final String TASK_ID = "task_id";

    public static final String CURRENT_APPROVE_ID = "current_approve_id";

    public static final String CURRENT_TASK_START_TIME = "current_task_start_time";

    public static final String TASK_STATUS = "task_status";

    public static final String TIMEOUT_WARN_STATUS = "timeout_warn_status";

    public static final String TIMEOUT_WARN_INTERVAL = "timeout_warn_interval";

    public static final String TIMEOUT_HANDLE_INTERVAL = "timeout_handle_interval";

    public static final String TIMEOUT_HANDLE_TYPE = "timeout_handle_type";

    public static final String APPROVE_TIME = "approve_time";

    public static final String REMARK = "remark";



    public ProcessTaskManagementEntity(String processInstanceId, String activityId, String taskId, LocalDateTime startTime, ApproveStatusEnum approveStatus, CamundaDTO.PropertiesDTO propertiesDTO, String userId) {
        this.processInstanceId = processInstanceId;
        this.currentNodeId = activityId;
        this.taskId = taskId;
        this.startTime = startTime;
        this.taskStatus = approveStatus;
        this.timeoutInterval = StrUtil.isNotBlank(propertiesDTO.getTimeoutInterval()) ? Integer.parseInt(propertiesDTO.getTimeoutInterval()) : 0;
        this.timeoutHandleType = propertiesDTO.getTimeoutHandling();
        this.timeoutWarnInterval = StrUtil.isNotBlank(propertiesDTO.getTimeoutWarnInterval()) ? Integer.parseInt(propertiesDTO.getTimeoutWarnInterval()) : 0;
        this.currentApproveId = userId;
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
