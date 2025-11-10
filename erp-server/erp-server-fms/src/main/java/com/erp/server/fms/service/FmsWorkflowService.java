package com.erp.server.fms.service;

import com.common.business.dto.ApproveDTO;
import com.erp.model.workflow.dto.EndProcessDTO;

/**
 * FMS工作流服务接口
 * @author System
 * @date 2025/11/10
 */
public interface FmsWorkflowService {

    /**
     * 审核结束处理
     * @param dto 审核结束参数
     * @return 处理结果
     */
    Boolean approveEnd(EndProcessDTO dto);

    /**
     * 反审核处理
     * @param dto 反审核参数
     * @return 处理结果
     */
    Boolean disApprove(ApproveDTO.DisApproveDTO dto);

    /**
     * 撤销流程处理
     * @param dto 撤销流程参数
     * @return 处理结果
     */
    Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto);
}
