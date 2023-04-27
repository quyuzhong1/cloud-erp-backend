package com.erp.server.workflow.service;

import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
public interface ProcessManagementService extends SuperService<ProcessManagementEntity> {

    /**
     * 启动流程
     * @param dto
     * @return
     */
    ProcessManagementDTO.StartResultDTO startProcess(ProcessManagementDTO.StartDTO dto);

    /**
     * 流程审核
     * @param dto
     */
    void approveProcess(ProcessManagementDTO.ApproveDTO dto);
}
