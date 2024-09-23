package com.erp.server.workflow.service;

import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.common.business.service.SuperService;
import com.erp.model.workflow.enums.TimeoutStatusEnum;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
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
public interface ProcessTaskManagementService extends SuperService<ProcessTaskManagementEntity> {

    /**
     * 更新审批状态
     *
     * @param taskId
     * @param approveType
     * @param comment
     * @param activityId
     * @param managementEntity
     * @param variablesMap
     * @return Boolean
     */
    Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String comment, String activityId, ProcessManagementEntity managementEntity, @Nullable Map<String, Object> variablesMap);

    /**
     * 根据流程实例id查询历史任务
     *
     * @param processInstanceId
     * @param num 返回任务数量
     * @return 以节点id为key，任务列表为value的map
     */
    LinkedHashMap<String, List<ProcessTaskManagementEntity>> listHisByProcessInstanceId(String processInstanceId, Integer num);

    /**
     * 保存流程任务
     * @param insertTask
     */
    ProcessTaskManagementEntity saveProcessTask(ProcessTaskManagementEntity insertTask);

    /**
     * 更新流程任务
     *
     * @param taskId
     * @param targetUserId
     * @param sourceUserId
     * @param remark
     */
    void updateTransfer(String taskId, String targetUserId, String targetUserName, String sourceUserId, String remark);

    /**
     * 根据流程实例id删除流程任务
     * @param processInstanceId
     */
    void removeByProcessInstanceId(String processInstanceId);

    /**
     * 更新超时状态
     * @param taskManagementIds
     * @param timeoutStatusEnum
     */
    void updateTimeoutStatus(List<String> taskManagementIds, TimeoutStatusEnum timeoutStatusEnum);

    /**
     * 根据流程实例id查询最近一条任务
     * @param processInstanceId
     * @return
     */
    ProcessTaskManagementEntity lastTask(String processInstanceId);

    /**
     * 根据业务id获取任务实例信息
     * @param businessIds
     * @return
     */
    List<ProcessTaskManagementEntity> listProcessByBusinessId(List<String> businessIds);

    /**
     * 查询前置审批任务
     * @param taskManagementId
     * @param processInstanceId
     * @return
     */
    List<ProcessTaskManagementEntity> listPreActivityTask(String taskManagementId, String processInstanceId);

    /**
     * 根据业务ID查询审核记录
     */
    List<ProcessTaskManagementDTO.ApproveHistoryDTO> listApproveHistory(String businessId);

    /**
     * 根据BusinessKey,taskStatus,curApproveId获取流程信息
     * @Author jack
     * @Date 2024/9/19
     * @param dto
     * @return List<ProcessTaskManagementEntity>
     **/
    List<ProcessTaskManagementEntity> listProcessByBusinessKey(ProcessManagementDTO.TaskKeyInfoDTO dto);
}
