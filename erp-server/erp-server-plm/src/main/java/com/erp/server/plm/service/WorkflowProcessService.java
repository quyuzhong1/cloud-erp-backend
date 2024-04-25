package com.erp.server.plm.service;

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
}
