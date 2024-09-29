package com.erp.server.workflow.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;

import java.util.List;
import java.util.Map;

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
    ProcessManagementDTO.ApproveResultDTO approveProcess(ProcessManagementDTO.ApproveDTO dto, Boolean isFirst);

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
     * @param comment
     * @param variablesMap
     * @return
     */
    Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String managementId, String processInstanceId, String comment, Map<String, Object> variablesMap);

    /**
     * 执行流程开始监听服务处理
     *
     * @param act
     * @param processInstanceId
     * @param startUserId
     */
    List<String> getCandidateByAct(PvmActivity act, String processInstanceId, String startUserId);

    /**
     * 退回流程
     * @param dto
     */
    ProcessManagementDTO.BackResultDTO back(ProcessManagementDTO.BackDTO dto);

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
    Boolean transfer(ProcessManagementDTO.TransferDTO dto);

    /**
     * 流程撤销
     * @param dto
     */
    ProcessManagementDTO.RevokeResultDTO revoke(ProcessManagementDTO.RevokeDTO dto);

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
     *
     * @param dto
     */
    void export(ProcessManagementDTO.ExportDTO dto);

    /**
     * 流程进度
     * @param dto
     */
    ProcessManagementDTO.ProcessResultDTO progress(ProcessManagementDTO.ProgressDTO dto);

    /**
     * 获取未发送任务
     */
    List<ProcessManagementDTO.ManagementTaskDTO> listUnsendTask(String taskId, String timeoutStatus);

    /**
     * 发送超时提醒
     * @param task
     */
    void sendTimeoutWarn(ProcessManagementDTO.ManagementTaskDTO task);

    /**
     * 发送超时处理
     * @param task
     */
    void sendTimeoutHandle(ProcessManagementDTO.ManagementTaskDTO task);

    /**
     * 批量转办
     * @param dto
     * @return
     */
    Boolean transferBatch(ProcessManagementDTO.TransferBatchDTO dto);

    /**
     * 根据id查询任务列表
     * @param ids
     * @return List<ProcessManagementDTO.ManagementTaskDTO>
     */
    List<ProcessManagementDTO.ManagementTaskDTO> listTaskById(List<String> ids);

    /**
     * 执行流程结束监听服务处理
     * @param processInstanceId
     */
    Boolean endExecutionHandle(String processInstanceId);

    /**
     * 批量启动流程
     * @param dto
     * @return
     */
    List<ProcessManagementDTO.StartResultDTO> batchStartProcess(ValidList<ProcessManagementDTO.StartDTO> dto);

    /**
     * 批量审批流程
     * @param dto
     * @return
     */
    List<ProcessManagementDTO.ApproveResultDTO> batchApproveProcess(ValidList<ProcessManagementDTO.ApproveDTO> dto);

    /**
     * 批量查询当前审批人
     * @param dtoList
     * @return
     */
    List<ProcessManagementDTO.CurApproveInfoDTO> batchCurApprover(ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList);

    /**
     * 批量查询当前待审核业务单据
     * @param dtoList
     * @return
     */
    List<ProcessManagementDTO.CurApproveInfoDTO> batchCurApproverByApprove(ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList);

    /**
     * 根据流程实例ID查询
     * @Author Luo_WG
     * @Date 2023/7/4 19:37
     * @param processInstanceId
     * @return com.erp.model.workflow.entity.ProcessManagementEntity
     **/
    ProcessManagementEntity getByProcessInstanceId(String processInstanceId);

    /**
     *
     * @param dto
     */
    void sameApproverAutoPass(ProcessManagementDTO.ApproveDTO dto, String processDefinitionId,String ProcessInstanceId);

    PagingVO<ProcessManagementDTO.PagingResultDTO> exportProcessManagement(PagingDTO<ProcessManagementDTO.ExportDTO> dto);
}
