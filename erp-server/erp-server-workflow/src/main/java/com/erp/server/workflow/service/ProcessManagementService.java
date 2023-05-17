package com.erp.server.workflow.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.common.business.service.SuperService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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
    void back(ProcessManagementDTO.BackDTO dto);

    /**
     * 根据业务id获取任务
     * @param businessId
     * @param businessKey
     * @param userId
     * @return
     */
    ProcessManagementDTO.ManagementTaskDTO getTaskByBusiness(String businessId, String businessKey, String userId);

    /**
     * 更新审批状态
     *
     * @param taskId
     * @param managementId
     * @param activityId
     * @param comment
     * @return
     */
    Boolean backUpdateApprove(String taskId, String managementId, ApproveTypeEnum approveType, String activityId, String comment);

    /**
     * 转办
     * @param dto
     */
    void transfer(List<ProcessManagementDTO.TransferDTO> dto);

    /**
     * 流程撤销
     * @param dto
     */
    void revoke(ProcessManagementDTO.RevokeDTO dto);

    /**
     * 根据流程实例id删除任务
     * @param processInstanceId
     * @return
     */
    Boolean removeByProcessInstanceId(String processInstanceId);

    /**
     * 根据业务查询历史任务
     * @param dto
     * @return
     */
    List<ProcessManagementDTO.HistoryActivityResultDTO> historyActivity(ProcessManagementDTO.HistoryActivityDTO dto);

    /**
     * 分页查询
     * @param pageDTO
     * @return PagingVO<ProcessManagementDTO.PagingResultDTO>
     */
    PagingVO<ProcessManagementDTO.PagingResultDTO> paging(PagingDTO<ProcessManagementDTO.SearchDTO> pageDTO);

    /**
     * 导出
     * @param dto
     */
    void export(ProcessManagementDTO.SearchDTO dto, HttpServletResponse response) throws Exception ;

    /**
     * 流程进度
     * @param dto
     */
    void progress(ProcessManagementDTO.ProgressDTO dto);
}
