package com.erp.model.workflow.dto;

import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

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
     * 审核平台，默认erp
     */
    private ApprovePlatformEnum approvePlatformEnum = ApprovePlatformEnum.ERP;

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
     *发货日期
     */
    private LocalDate deliveryDate;

    /**
     * 最近任务审批意见
     */
    private String comment;

    /**
     * 流程参数map
     */
    private Map<String,Object> variablesMap;

    public EndProcessDTO(ProcessManagementDTO.RevokeDTO dto) {
        this.businessKey = dto.getBusinessKey();
        this.businessId = dto.getBusinessId();
        this.approveStatus = ApproveTypeEnum.REVOKE;
        this.approveUserId = dto.getUserId();
        this.approveTime = LocalDateTime.now();
        this.comment = dto.getRemark();
    }

    public EndProcessDTO(ProcessManagementEntity entity, String approveTypeCode, LocalDateTime approveTime, String lastApprover, String comment, LocalDate deliveryDate, Map<String,Object> variablesMap) {
        this.businessKey = entity.getBusinessKey();
        this.businessId = entity.getBusinessId();
        this.approveStatus = ApproveTypeEnum.getByCode(approveTypeCode);
        this.approveUserId = lastApprover;
        this.approveTime = approveTime;
        this.comment = comment;
        this.deliveryDate = deliveryDate;
        this.variablesMap = variablesMap;
    }
}
