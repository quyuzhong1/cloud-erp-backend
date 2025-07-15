package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.erp.model.workflow.dto.EndProcessDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 流程业务层
 * @date 2023/8/2 16:04
 */
public interface WorkflowProcessService {

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/8/2 16:13
     * @param dto
     * @return Boolean
     */
    Boolean approveEnd(EndProcessDTO dto);


    /**
     * 反审核
     * @author will
     * @date 2025/6/18 09:32
     * @param dto
     * @return Boolean
     */
    Boolean disApprove(ApproveDTO.DisApproveDTO dto);
    /**
     * 撤销流程
     * @author will
     * @date 2025/6/18 10:41
     * @param dto
     * @return Boolean
     */
    Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto);
}
