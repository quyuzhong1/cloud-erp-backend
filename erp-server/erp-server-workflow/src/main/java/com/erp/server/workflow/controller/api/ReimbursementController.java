package com.erp.server.workflow.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @Classname ReimbursementController

 * @Date 2022-08-10 14:08
 * @Created by yl
 */
@RestController
@RequestMapping("/")
@Slf4j
public class ReimbursementController extends BaseController {


    @Resource
    private WorkflowService workflowService;


    @Resource
    private RepositoryService repositoryService;
    @Resource
    private ProcessTaskService processTaskService;

    //删除流程
    @PostMapping("/removeProcess")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除流程")
    public ApiResult<Objects> removeProcess(@RequestParam(value = "deploymentId")  String  deploymentId) {
        repositoryService.deleteDeployment(deploymentId);
        return success();
    }


    //启动流程
    @PostMapping("/startProcess")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "启动流程")
    public ApiResult<Objects> startProcess(@RequestBody @Validated StartProcessDTO dto) {
        workflowService.startProcess(dto);
        return success();
    }

    //查看任务
    @GetMapping("/queryMyToDo")
    public ApiResult<List<TaskShowDTO>> queryMyToDo(String userId) {
        List<TaskShowDTO> list = processTaskService.queryMyToDo(userId);
        return success(list);
    }

    //审批通过任务
    @PostMapping("/taskPass")
    @LogAction(value = LogActionEnum.APPROVE, desc = "任务审核通过")
    public ApiResult<Objects> taskPass(@RequestBody @Validated ApproveProcessDTO dto) {
       processTaskService.taskPass(dto);
       return success();
    }


    // 删除任务 不一定删除成功
    @PostMapping("/removeTask")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除任务")
    public ApiResult<Objects> removeTask(String taskId) {
        processTaskService.removeTask(taskId);
        return success();
    }


    //驳回到上一级
    @PostMapping("/rejectGoBack")
    @LogAction(value = LogActionEnum.REJECT, desc = "驳回到上一级")
    public ApiResult<Objects> rejectGoBack(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.rejectGoBackProcess(dto);
        return success();
    }

    //驳回到起点
    @PostMapping("/rejectOrigin")
    @LogAction(value = LogActionEnum.REJECT, desc = "驳回到起点")
    public ApiResult<Objects> rejectOrigin(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.rejectOriginProcess(dto);
        return success();
    }

    //撤销流程
    @PostMapping("/withDraw")
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销流程")
    public ApiResult<Objects> withDraw(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.withDrawProcess(dto);
        return success();
    }

    //撤回流程
    @PostMapping("/fetchBack")
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤回流程")
    public ApiResult<Objects> fetchBackProcess(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.fetchBackProcess(dto);
        return success();
    }

    //任务历史
    @GetMapping("/queryMyTaskHistory")
    public ApiResult<List<HistoricTaskInstance>> queryMyTaskHistory(QueryProcessDTO dto) {
        List<HistoricTaskInstance> list=processTaskService.historicTaskInstances(dto);
        return success(list);
    }

    //单个任务
    @GetMapping("/taskInfo")
    public ApiResult<TaskShowDTO> queryTaskInfo(@RequestBody @Validated ApproveProcessDTO dto) {
        TaskShowDTO vo=processTaskService.queryTaskInfo(dto);
        return success(vo);
    }

    //查看流程审批情况
    @GetMapping("/queryProcessApprove")
    public ApiResult<List<AuditorHandleDTO>> queryProcessApprove(@RequestBody @Validated ProcessBaseDTO dto) {
        List<AuditorHandleDTO> resultList=workflowService.queryApproveRecord(dto);
        return success(resultList);
    }


    /**
     *  根据 表id 查看流程审批情况
     */

    @GetMapping("/queryProcessApproveById")
    public ApiResult<List<ApproveNodeRecordVO>> queryProcessApprove(@RequestBody @Validated BaseIdDTO dto) {
        List<ApproveNodeRecordVO> resultList=workflowService.queryApproveRecordById(dto.getId());
        return success(resultList);
    }
}
