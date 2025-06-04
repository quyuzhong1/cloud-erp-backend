package com.erp.model.workflow.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.workflow.dto.CamundaDTO;
import com.erp.model.workflow.enums.TimeoutStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

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
@TableName(value = "process_task_management")
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
    @TableField("cur_activity_id")
    private String curActivityId;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 当前审批人ID
     */
    @TableField("cur_approve_id")
    private String curApproveId;

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
    @TableField("timeout_status")
    private TimeoutStatusEnum timeoutStatus;

    /**
     * 超时预警时间
     */
    @TableField("timeout_warn_time")
    private LocalDateTime timeoutWarnTime;

    /**
     * 超时时间
     */
    @TableField("timeout_handle_time")
    private LocalDateTime timeoutHandleTime;

    /**
     * 超时处理方式
     */
    @TableField("timeout_handle_type")
    private String timeoutHandleType;

    /**
     * 审批时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 上一节点ID
     */
    @TableField("pre_activity_id")
    private String preActivityId;

    @TableField("execution_id")
    private String executionId;

    @TableField("approve_id")
    private String approveId;

    @TableField("cur_activity_name")
    private String curActivityName;

    @TableField("cur_approve_name")
    private String curApproveName;

    @TableField("approve_name")
    private String approveName;

    @TableField(exist = false)
    private String businessId;

    /**
     * 原审批人id（委托审批后存原始值）
     */
    @TableField("original_approve_id")
    private String originalApproveId;

    /**
     * 标识json
     */
    @TableField(value = "label_json", jdbcType = JdbcType.OTHER)
    private JSONObject labelJson;

    public static final String PROCESS_INSTANCE_ID = "process_instance_id";

    public static final String TASK_ID = "task_id";
    public static final String TASK_STATUS = "task_status";

    public static final String TIMEOUT_STATUS = "timeout_status";

    public static final String TIMEOUT_WARN_INTERVAL = "timeout_warn_interval";

    public static final String TIMEOUT_HANDLE_INTERVAL = "timeout_handle_interval";

    public static final String TIMEOUT_HANDLE_TYPE = "timeout_handle_type";

    public static final String APPROVE_TIME = "approve_time";

    public static final String FIELD_REMARK = "remark";

    public static final String PRE_ACTIVITY_ID = "pre_activity_id";

    public static final String EXECUTION_ID = "execution_id";

    public static final String APPROVE_ID = "approve_id";



    public ProcessTaskManagementEntity(String processInstanceId, String activityId, String taskId, LocalDateTime startTime, ApproveStatusEnum approveStatus, CamundaDTO.PropertiesDTO propertiesDTO, FindUserDTO findUserDTO, String executionId, String activityName,JSONObject labelJson) {
        this.processInstanceId = processInstanceId;
        this.curActivityId = activityId;
        this.taskId = taskId;
        this.startTime = startTime;
        this.taskStatus = approveStatus;
        this.timeoutHandleTime = CharSequenceUtil.isNotBlank(propertiesDTO.getTimeoutInterval()) ? LocalDateUtil.plusHours(startTime, propertiesDTO.getTimeoutInterval()) : startTime;
        this.timeoutHandleType = propertiesDTO.getTimeoutHandling();
        this.timeoutWarnTime = CharSequenceUtil.isNotBlank(propertiesDTO.getTimeoutInterval()) ? LocalDateUtil.plusHours(startTime, propertiesDTO.getTimeoutWarnInterval()) : startTime;
        this.curApproveId = findUserDTO.getUserId();
        this.curApproveName = findUserDTO.getUserName();
        this.executionId = executionId;
        this.curActivityName = activityName;
        this.labelJson = labelJson;
    }

    public static ProcessTaskManagementEntity getByEntity(ProcessTaskManagementEntity entity, String targetUserId, String targetUserName) {
        ProcessTaskManagementEntity insertEntity = new ProcessTaskManagementEntity();
        BeanUtil.copyProperties(entity, insertEntity, "id","createTime","updateTime","version");
        insertEntity.setStartTime(LocalDateTime.now());
        insertEntity.setCurApproveId(targetUserId);
        insertEntity.setCurApproveName(targetUserName);
        Duration warnDuration = Duration.between(entity.getStartTime(), entity.getTimeoutWarnTime());
        insertEntity.setTimeoutWarnTime(insertEntity.getStartTime().plusHours(warnDuration.toHours()));
        Duration duration = Duration.between(entity.getStartTime(), entity.getTimeoutHandleTime());
        insertEntity.setTimeoutHandleTime(insertEntity.getStartTime().plusHours(duration.toHours()));
        return insertEntity;
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
