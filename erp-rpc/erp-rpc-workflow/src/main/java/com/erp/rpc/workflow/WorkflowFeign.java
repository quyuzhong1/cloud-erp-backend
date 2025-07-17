package com.erp.rpc.workflow;

import com.common.business.config.ExportFeignConfig;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.model.workflow.vo.ProcessCurrentAuditorVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * @Classname WorkflowFeign

 * @Date 2022-10-18 17:00
 * @Created by yl
 */
@FeignClient(name ="erp-workflow", contextId = "workflowFeign", configuration = ExportFeignConfig.class)
public interface WorkflowFeign {


    /**
     * 启动流程(弃用)
     * @deprecated
     */
    @Deprecated
    @PostMapping("feign/process/startProcess")
    ProcessNodeDTO startProcess(@RequestBody StartProcessDTO startProcessDTO);

    //根据人员获取我待办的任务列表
    @PostMapping("feign/process/queryMyToDo")
    List<TaskShowDTO> queryMyToDo(@RequestParam(value="userId") String userId);

    /**
     * 根据用户id 获取用用户所需要的待办的任务列表
     * @author yl
     * @date 2023-01-31 10:59
     * @param userId
     * @return java.util.List<com.erp.model.workflow.vo.MyToDoTaskVO>
     */
    @PostMapping("feign/process/getMyToDoTasks")
    List<MyToDoTaskVO> getMyToDoTasks(@RequestParam(value="userId") String userId);


    /**
     * 审核任务通过(弃用)
     * @deprecated
     */
    @Deprecated
    @PostMapping("feign/process/taskPass")
    ProcessNodeDTO taskPass(@RequestBody ApproveProcessDTO dto);


    /**
     * 审核任务不通过(弃用)
     * @deprecated
     */
    @Deprecated
    @PostMapping("feign/process/taskNoPass")
    ProcessNodeDTO taskNoPass(@RequestBody ApproveProcessDTO dto);

    //撤销流程
    @PostMapping("feign/process/withDraw")
    void withDraw(@RequestBody ApproveProcessDTO dto);

    /**
     * 取回流程(弃用)
     * @deprecated
     */
    @Deprecated
    @PostMapping("feign/process/fetchBack")
    void fetchBack(@RequestBody ApproveProcessDTO dto);


    /**
     * 取回起始点(弃用)
     * @deprecated
     */
    @Deprecated
    @PostMapping("feign/process/rejectOrigin")
    void rejectOrigin(@RequestBody ApproveProcessDTO dto);

    /**
     * 终止流程(弃用)
     * @deprecated
     */
    @Deprecated
    @PostMapping("feign/process/terminate")
    void terminate(@RequestBody ApproveProcessDTO dto);

    /**
     * 取消流程
     * @deprecated
     */
    @Deprecated
    @PostMapping("feign/process/cancelProcess")
    void cancelProcess(@RequestBody List<String> ids);

    //根据审核任务id获取我待办的任务列表
    @PostMapping("feign/process/queryMyToDoByTaskId")
    List<TaskShowDTO> queryMyToDoByTaskId(@RequestParam(value="taskId") String taskId);

    //获取审核记录
    @PostMapping("feign/process/getHistoryTaskByProcessId")
    List<AuditorHandleDTO> getHistoryTaskByProcessId(@RequestParam(value="processId") String processId);

    /**
     * 获取 业务信息
     * @author yl
     * @date 2023-01-31 15:31
     * @param findProcess
     * @return com.erp.model.workflow.dto.WorkflowBusinessDTO
     */
    @PostMapping("feign/process/getBusiness")
    BusinessInfoDTO getBusiness(@RequestBody FindProcessDTO findProcess);

    /**
     * 保存 业务流程关系表
     * @author yl
     * @date 2023-01-31 17:13
     * @param businessProcess
     * @return void
     */
    @PostMapping("feign/process/saveBusinessProcess")
    Boolean saveBusinessProcess(@RequestBody WorkflowBusinessProcessDTO businessProcess);

    @PostMapping("feign/process/getProcessByBusinessTable")
    MyToDoTaskVO getByBusinessTableId(@RequestBody BusinessTableDTO  dto);

    @PostMapping("feign/process/getProcess")
    List<WorkflowBusinessProcessDTO> getProcess(@RequestBody List<String> businessTableIds);

    /**
     * 根据业务表获取审核记录
     * @author yl
     * @date 2023-02-11 11:32
     * @param id
     * @return java.util.List<com.erp.model.workflow.dto.ApproveRecordShowDTO>
     */
    @PostMapping("feign/process/getHistoryTaskByBusinessTableId")
    List<ApproveNodeRecordVO> getHistoryTaskByBusinessTableId(@RequestBody String id);


    /**
     * 根据业务表id获取当前审核人情况
     * @param businessTableIds
     * @return
     */
    @PostMapping("feign/process/getProcessCurrentAudit")
    List<ProcessCurrentAuditorVO> getProcessCurrentAudit(@RequestBody List<String> businessTableIds);


    /**
     * 根据业务表id获取当前审核人情况
     * @param businessTableId
     * @return
     */
    @PostMapping("feign/process/getProcessNextAudit")
    ProcessCurrentAuditorVO getProcessNextAudit(@RequestBody String businessTableId);


    /**
     * 根据业务表id 撤销流程
     * @param dto
     * @return
     */
    @PostMapping("feign/process/withDrawByBusiness")
    Boolean withDrawByBusiness(@RequestBody WithDrawProcessBusinessDTO dto);

    /**
     * 根据业务表id 撤销流程
     * @param processIdList
     * @return
     */
    @PostMapping("feign/process/batchCancelProcess")
    Boolean batchCancelProcess(@RequestBody List<String> processIdList);

    /**
     * 流程启动 -new
     */
    @PostMapping("feign/process/start")
    ApiResult<ProcessManagementDTO.StartResultDTO> start(@RequestBody ProcessManagementDTO.StartDTO dto);

    /**
     * 流程审批 -new
     */
    @PostMapping("feign/process/approve")
    ApiResult<ProcessManagementDTO.ApproveResultDTO> approve(@RequestBody ProcessManagementDTO.ApproveDTO dto);

    /**
     * 流程驳回到指定节点 -new
     */
    @PostMapping("feign/process/back")
    ApiResult<ProcessManagementDTO.BackResultDTO> backProcess(@RequestBody @Valid ProcessManagementDTO.BackDTO dto);

    /**
     * 流程取回 -new
     */
    @PostMapping("feign/process/revoke")
    ApiResult<ProcessManagementDTO.RevokeResultDTO> revokeProcess(@RequestBody @Valid ProcessManagementDTO.RevokeDTO dto);

    /**
     * 转发任务 - new
     */
    @PostMapping("feign/process/transfer")
    ApiResult<Boolean> transferProcess(@RequestBody @Valid ProcessManagementDTO.TransferDTO dto);

    /**
     * 历史流程节点 - 用于指定人驳回
     */
    @PostMapping("feign/process/history/activity")
    ApiResult<List<ProcessManagementDTO.HistoryActivityResultDTO>> historyActivity(@RequestBody @Valid ProcessManagementDTO.HistoryActivityDTO dto);

    /**
     * 批量启动流程
     * @param dto
     * @return
     */
    @PostMapping("feign/process/batchStart")
    ApiResult<List<ProcessManagementDTO.StartResultDTO>> batchStartProcess(@RequestBody @Valid ValidList<ProcessManagementDTO.StartDTO> dto);

    /**
     * 批量审批流程
     * @param dto
     * @return
     */
    @PostMapping("feign/process/batchApprove")
    ApiResult<List<ProcessManagementDTO.ApproveResultDTO>> batchApproveProcess(@RequestBody @Valid ValidList<ProcessManagementDTO.ApproveDTO> dto);

    /**
     * 批量查询流程当前审批人
     * @param dtoList
     * @return
     */
    @PostMapping("/feign/process/batchCurApprover")
    ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> curApprover(@RequestBody @Valid ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList);

    /**
     * 批量查询当前待审核业务单据
     * @param dtoList
     * @return
     */
    @PostMapping("/feign/process/batchCurApproverByApprove")
    ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> batchCurApproverByApprove(@RequestBody @Valid ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList);

    /**
     * 根据流程id 获取审核情况
     * @param processId
     * @return
     */
    @PostMapping("/feign/process/listHistoryTaskByProcessId")
    List<ApproveNodeRecordVO> listHistoryTaskByProcessId(String processId);

    /**
     * 根据业务id获取流程实例信息
     */
    @PostMapping("/feign/process/listProcessByProcessId")
    List<ProcessTaskManagementEntity> listProcessByBusinessId(@RequestBody List<String> businessIds);

    /**
     * 根据业务类型查询流程信息
     */
    @PostMapping("/feign/process/getProcessBusiness")
    ProcessBusinessEntity getProcessBusiness(@RequestBody String businessKey);

    /**
     * 根据BusinessKey,taskStatus,curApproveId获取流程信息
     */
    @PostMapping("/feign/process/listProcessByBusinessKey")
    List<ProcessTaskManagementEntity> listProcessByBusinessKey(@RequestBody ProcessManagementDTO.TaskKeyInfoDTO dto);

    /**
     * 根据BusinessKey,businessId判断当前单据是否提审操作
     */
    @PostMapping("/feign/process/checkSubmitByBusinessId")
    Boolean checkSubmitByBusinessId(@RequestBody ProcessManagementDTO.CheckSubmitByBusinessIdDTO dto);
}
