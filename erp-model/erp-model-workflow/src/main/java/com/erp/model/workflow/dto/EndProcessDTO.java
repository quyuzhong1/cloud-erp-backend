package com.erp.model.workflow.dto;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流程结束返回参数信息
 *
 * @Author Cloud
 * @Date 2023/6/27 19:06
 **/

@NoArgsConstructor
@Data
public class EndProcessDTO {

    /**
     * 业务key
     */
    private String businessKey;

    /**
     * 业务id
     */
    private String businessId;

    /**
     * 审批结果
     */
    private ApproveTypeEnum approveStatus;

    /**
     * 最近任务审批人
     */
    private String approveUserId;

    /**
     * 最近任务审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 最近任务审批意见
     */
    private String comment;

    public EndProcessDTO(ProcessManagementEntity entity, ProcessTaskManagementEntity taskEntity, String approveTypeCode) {
        this.businessKey = entity.getBusinessKey();
        this.businessId = entity.getBusinessId();
        this.approveStatus = ApproveTypeEnum.getByCode(approveTypeCode);
        this.approveUserId = taskEntity.getApproveId();
        this.approveTime = taskEntity.getApproveTime();
        this.comment = taskEntity.getRemark();
    }
}
