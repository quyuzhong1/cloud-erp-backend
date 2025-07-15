package com.erp.server.workflow.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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


    ProcessManagementDTO.StartResultDTO startProcessManagement(ProcessManagementDTO.StartDTO dto);

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
     * @param executionDelegate
     * @param startUserId
     */
    List<String> getCandidateByAct(PvmActivity act, DelegateExecution executionDelegate, String startUserId);

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
     * 审核结果回调
     * @author will
     * @date 2025/6/18 09:23
     * @param businessKey
     * @param dto
     * @return Boolean
     */
    Boolean callFeign(String businessKey, EndProcessDTO dto);

    /**
     * 反审核通过回调
     * @author will
     * @date 2025/6/18 09:24
     * @param dto
     * @return Boolean
     */
    Boolean disApproveFeign(ApproveDTO.DisApproveDTO dto);

    /**
     * 取消流程回调
     * @author will
     * @date 2025/6/18 09:24
     * @param dto
     * @return Boolean
     */
    Boolean cancelProcessFeign(ApproveDTO.CancelProcessDTO dto);
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
    void sameApproverAutoPass(ProcessManagementDTO.ApproveDTO dto, String processDefinitionId,String processInstanceId);

    /**
     * 更新业务单据状态
     * @author will
     * @date 2025/5/22 11:15
     * @param entity
     * @param typeEnum
     * @param comment
     * @return Boolean
     */
    Boolean updateBusinessStatus(ProcessManagementEntity entity,ApproveTypeEnum typeEnum,String comment);
    /**
     * 流程强制通过
     * @author will
     * @date 2025/5/15 17:27
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO processPass(String id);
    /**
     * 流程强制驳回
     * @author will
     * @date 2025/5/15 17:27
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO processReject(String id);
    /**
     * 恢复
     * @author will
     * @date 2025/5/15 17:28
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO processRestore(String id);
    /**
     * 暂停
     * @author will
     * @date 2025/5/15 17:28
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO processSuspend(String id);
    /**
     * tab列表
     * @author will
     * @date 2025/5/15 17:57
     * @param dto
     * @return List<TabListDTO>
     */
    List<ProcessManagementDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * 查询明细信息
     * @author will
     * @date 2025/6/4 10:48
     * @param dto
     * @return List<DetailPagingResultDTO>
     */
    List<ProcessManagementDTO.DetailPagingResultDTO> listDetail(ProcessManagementDTO.DetailSearchDTO dto);
    /**
     * 主表分页信息
     * @author will
     * @date 2025/6/4 14:43
     * @param dto
     * @return PagingVO<MainPagingResultDTO>
     */
    PagingVO<ProcessManagementDTO.MainPagingResultDTO> mainPaging(PagingDTO<ProcessManagementDTO.SearchDTO> dto);

    List<String> getTestList(String businessType);

    Boolean checkSubmitByBusinessId(ProcessManagementDTO.CheckSubmitByBusinessIdDTO dto);

    Boolean checkTaskByProcessInstanceId(String processInstanceId);
    /**
     * 查询进行中的数据
     * @author will
     * @date 2025/6/30 11:18
     * @param processDefinitionIdList
     * @param processDefinitionVersionList
     * @return List<ProcessManagementEntity>
     */
    List<ProcessManagementEntity> listDoing(List<String> processDefinitionIdList, List<Integer> processDefinitionVersionList);
}
