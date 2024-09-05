package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程任务管理DTO
 * @date 2024-09-05
 * @author tanmujin
 */
@Data
public class ProcessTaskManagementDTO implements Serializable {

    /**
     * 审核记录DTO
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ApproveHistoryDTO extends CommonDTO{
        /**
         * 附件
         */
        private List<ProcessTaskManagementAttachmentDTO.CommonDTO> attachmentList;
    }

    @Data
    public static class CommonDTO{
        /**
         * 业务ID
         */
        private String businessId;
        /**
         * 审核任务ID
         */
        private String taskId;
        /**
         * 审核人ID
         */
        private String approveUserId;
        /**
         * 审核人名称
         */
        private String approveUserName;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
        /**
         * 备注
         */
        private String remark;
        /**
         * 审核意见
         */
        private String comment;
    }
}
