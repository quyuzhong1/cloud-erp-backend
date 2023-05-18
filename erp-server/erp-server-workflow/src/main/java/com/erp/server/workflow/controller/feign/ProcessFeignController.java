package com.erp.server.workflow.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.model.workflow.vo.ProcessCurrentAuditorVO;
import com.erp.server.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Classname ProcessFeignController
 * @Description TODO
 * @Date 2022-10-17 10:34
 * @Created by yl
 */

@RestController
@RequestMapping("feign/process")
@Slf4j
public class ProcessFeignController extends BaseController {

    @Autowired
    public WorkflowService workflowService;

    @Autowired
    private ProcessTaskService processTaskService;

    @Autowired
    private WorkflowBusinessService businessService;

    @Autowired
    private WorkflowBusinessProcessService businessProcessService;
    @Resource
    private ProcessManagementService processManagementService;


    //启动流程
    @PostMapping("/startProcess")
    public ProcessNodeDTO startProcess(@RequestBody StartProcessDTO dto) {
        ProcessNodeDTO process = workflowService.startProcess(dto);
        return process;
    }


    //根据人员查看任务
    @PostMapping("/queryMyToDo")
    public List<TaskShowDTO> queryMyToDo(String userId) {
        List<TaskShowDTO> list = processTaskService.queryMyToDo(userId);
        return list;
    }

    //根据人员查看任务
    @PostMapping("/getMyToDoTasks")
    public List<MyToDoTaskVO> getMyToDoTasks(String userId) {
        List<MyToDoTaskVO> list = processTaskService.getMyToDoTasks(userId);
        return list;
    }

    //审核通过任务
    @PostMapping("/taskPass")
    public ProcessNodeDTO taskPass(@RequestBody @Validated ApproveProcessDTO dto) {
        ProcessNodeDTO node = processTaskService.taskPass(dto);
        return node;
    }

    //审核不通过任务
    @PostMapping("/taskNoPass")
    public ProcessNodeDTO taskNoPass(@RequestBody @Validated ApproveProcessDTO dto) {
        ProcessNodeDTO node = processTaskService.taskNoPass(dto);
        return node;
    }

    //回退至初始状态
    @PostMapping("/rejectOriginProcess")
    public void rejectOriginProcess(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.rejectOriginProcess(dto);
    }

    //撤销流程
    @PostMapping("/withDraw")
    public void withDraw(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.withDrawProcess(dto);
    }

    //取回流程
    @PostMapping("/fetchBack")
    public void fetchBack(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.fetchBackProcess(dto);
    }

    //终止流程
    @PostMapping("/terminate")
    public void terminate(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.terminateProcess(dto.getProcessInstanceId());
    }

    //取消流程
    @PostMapping("/cancelProcess")
    public void cancelProcess(@RequestBody List<String> ids) {
        workflowService.cancelProcess(ids);
    }

    //驳回到源点
    @PostMapping("/rejectOrigin")
    public void rejectOrigin(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.rejectOriginProcess(dto);
    }


    //根据审核任务id查看任务
    @PostMapping("/queryMyToDoByTaskId")
    public List<TaskShowDTO> queryMyToDoByTaskId(String taskId) {
        List<TaskShowDTO> list = processTaskService.queryMyToDoByTaskId(taskId);
        return list;
    }

    //查看流程审批情况
    @PostMapping("/getHistoryTaskByProcessId")
    public List<AuditorHandleDTO> getHistoryTaskByProcessId(String processId) {
        List<AuditorHandleDTO> resultList = processTaskService.getHistoryTaskByProcessId(processId);
        return resultList;
    }

    /**
     * 根据业务表id获取审批情况
     *
     * @param businessTableId
     * @return
     * @author yl
     * @date 2023-02-11 11:33
     */
    //查看流程审批情况
    @PostMapping("/getHistoryTaskByBusinessTableId")
    public List<ApproveNodeRecordVO> getHistoryTaskByBusinessTableId(@RequestBody String businessTableId) {
        List<ApproveNodeRecordVO> list = processTaskService.getHistoryTaskByBusinessTableId(businessTableId);
        return list;
    }


    /**
     * 查看业务流程具体信息
     *
     * @param
     * @return
     * @author yl
     * @date 2023-01-31 15:32
     */
    @PostMapping("/getBusiness")
    public BusinessInfoDTO getWorkflowBusiness(@RequestBody FindProcessDTO findProcess) {
        BusinessInfoDTO business = businessService.getBusiness(findProcess);
        return business;
    }

    /**
     * 保存业务与流程的信息
     *
     * @param
     * @return
     * @author yl
     * @date 2023-01-31 15:32
     */
    @PostMapping("/saveBusinessProcess")
    public Boolean saveBusinessProcess(@RequestBody WorkflowBusinessProcessDTO dto) {
        Boolean result = businessProcessService.saveBusinessProcess(dto);
        return result;
    }


    @PostMapping("/getProcessByBusinessTable")
    public MyToDoTaskVO getProcessByBusinessTable(@RequestBody BusinessTableDTO dto) {
        MyToDoTaskVO result = businessProcessService.getProcessByBusinessTable(dto);
        return result;

    }


    /**
     * 根据业务表id集合 获取到对应 流程id
     *
     * @param businessTableIds
     * @return java.util.List<com.erp.model.workflow.dto.WorkflowBusinessProcessDTO>
     * @author yl
     * @date 2023-02-08 19:48
     */
    @PostMapping("/getProcess")
    public List<WorkflowBusinessProcessDTO> getProcess(@RequestBody List<String> businessTableIds) {
        List<WorkflowBusinessProcessDTO> list = businessProcessService.getProcessByTables(businessTableIds);
        return list;
    }


    /**
     * 根据业务表id集合 获取到对应流程当前审核人信息
     *
     * @param businessTableIds
     * @return java.util.List<com.erp.model.workflow.dto.WorkflowBusinessProcessDTO>
     * @author yl
     * @date 2023-02-08 19:48
     */
    @PostMapping("/getProcessCurrentAudit")
    public List<ProcessCurrentAuditorVO> getProcessCurrentAudit(@RequestBody List<String> businessTableIds) {
        List<ProcessCurrentAuditorVO> list = businessProcessService.getProcessCurrentAuditor(businessTableIds);
        return list;
    }


    /**
     * 根据业务表id 获取到 下一个 流程审核情况
     *
     * @param businessTableId
     * @return java.util.List<com.erp.model.workflow.dto.WorkflowBusinessProcessDTO>
     * @author yl
     * @date 2023-02-08 19:48
     */
    @PostMapping("/getProcessNextAudit")
    public ProcessCurrentAuditorVO getProcessNextAudit(@RequestBody String businessTableId) {
        ProcessCurrentAuditorVO result = businessProcessService.getProcessNextAudit(businessTableId);
        return result;
    }


    /**
     * 撤销流程
     */
    @PostMapping("/withDrawByBusiness")
    public Boolean withDraw(@RequestBody WithDrawProcessBusinessDTO dto) {
        return workflowService.withDrawProcessByBusinessTable(dto);
    }

    /**
     * 流程启动 -new
     */
    @PostMapping("/start")
    public ProcessManagementDTO.StartResultDTO start(@RequestBody ProcessManagementDTO.StartDTO dto) {
        return processManagementService.startProcess(dto);
    }

    /**
     * 流程审批 -new
     */
    @PostMapping("/approve")
    public ProcessManagementDTO.ApproveResultDTO approve(@RequestBody ProcessManagementDTO.ApproveDTO dto) {
        return processManagementService.approveProcess(dto);
    }

    /**
     * 流程驳回到指定节点 -new
     */
    @PostMapping("/back")
    public ApiResult<ProcessManagementDTO.BackResultDTO> backProcess(@RequestBody @Valid ProcessManagementDTO.BackDTO dto) {
        ProcessManagementDTO.BackResultDTO resultDTO = processManagementService.back(dto);
        return success(resultDTO);
    }

    /**
     * 流程取回 -new
     */
    @PostMapping("/revoke")
    public ApiResult<ProcessManagementDTO.RevokeResultDTO> revokeProcess(@RequestBody @Valid ProcessManagementDTO.RevokeDTO dto) {
        ProcessManagementDTO.RevokeResultDTO revokeResult = processManagementService.revoke(dto);
        return success(revokeResult);
    }

    /**
     * 转发任务 - new
     */
    @PostMapping("/transfer")
    public ApiResult<Boolean> transferProcess(@RequestBody @Valid @NotNull List<ProcessManagementDTO.TransferDTO> dto) {
        return success(processManagementService.transfer(dto));
    }

    /**
     * 历史流程节点 - 用于指定人驳回
     */
    @PostMapping("/history/activity")
    public ApiResult<List<ProcessManagementDTO.HistoryActivityResultDTO>> historyActivity(@RequestBody @Valid ProcessManagementDTO.HistoryActivityDTO dto) {
        List<ProcessManagementDTO.HistoryActivityResultDTO> resultList = processManagementService.historyActivity(dto);
        return success(resultList);
    }

}
