package com.erp.server.workflow.service;

import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.common.business.service.SuperService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;

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

    /**
     * 流程创建监听服务处理
     * @param taskDelegate
     */
    void createTaskHandle(DelegateTask taskDelegate);

    /**
     * 流程完成监听服务处理
     * @param taskDelegate
     */
    void completeTaskHandle(DelegateTask taskDelegate);

    /**
     * 更新审批状态
     *
     * @param taskId
     * @param approveType
     * @param managementId
     * @param processInstanceId
     * @return
     */
    Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String managementId, String processInstanceId, String remark);

    /**
     * 执行流程开始监听服务处理
     * @param executionDelegate
     */
    void startExecutionHandle(DelegateExecution executionDelegate);

    /**
     * 退回流程
     * @param dto
     */
    void rollback(ProcessManagementDTO.ApproveDTO dto);

//    /**
//     * 驳回流程
//     * @param dto
//     */
//    void rejectProcess(ProcessManagementDTO.ApproveDTO dto);
}
