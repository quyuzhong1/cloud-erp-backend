package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.enums.ProcessStatusEnum;
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
@TableName("process_management")
@NoArgsConstructor
public class ProcessManagementEntity extends BaseEntity<ProcessManagementEntity> {

    /**
     * 流程实例ID
     */
    @TableField("process_instance_id")
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    @TableField("process_definition_id")
    private String processDefinitionId;

    /**
     * 业务ID
     */
    @TableField("business_id")
    private String businessId;

    /**
     * 业务编码
     */
    @TableField("business_code")
    private String businessCode;

    /**
     * 当前节点ID
     */
    @TableField("cur_activity_id")
    private String curActivityId;

    /**
     * 流程状态
     */
    @TableField("process_status")
    private ProcessStatusEnum processStatus;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;
    /**
     * 业务名称
     */
    @TableField("business_name")
    private String businessName;

    /**
     * 审核状态 approveStatus
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;

    /**
     * 业务类型KEY
     */
    @TableField("business_key")
    private String businessKey;

    /**
     * 流程引擎流程实例ID
     */
    @TableField("act_process_definition_id")
    private String actProcessDefinitionId;


    public static final String PROCESS_INSTANCE_ID = "process_instance_id";

    public static final String PROCESS_DEFINITION_ID = "process_definition_id";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_CODE = "business_code";

    public static final String CUR_ACTIVITY_ID = "cur_activity_id";

    public static final String PROCESS_STATUS = "process_status";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String BUSINESS_NAME = "business_name";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String BUSINESS_KEY = "business_key";

    public static final String ACT_PROCESS_DEFINITION_ID = "act_process_definition_id";

    public ProcessManagementEntity(String processInstanceId, ProcessManagementDTO.StartDTO dto, String activityId, LocalDateTime startTime, String processDefinitionId, String definitionId) {
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
        this.businessId = dto.getBusinessId();
        this.businessCode = dto.getBusinessCode();
        this.curActivityId = activityId;
        this.startTime = startTime;
        this.processStatus = ProcessStatusEnum.RUNNING;
        this.approveStatus = ApproveStatusEnum.APPROVE_ING;
        this.businessName = dto.getBusinessName();
        this.businessKey = dto.getBusinessKey();
        this.actProcessDefinitionId = definitionId;
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
