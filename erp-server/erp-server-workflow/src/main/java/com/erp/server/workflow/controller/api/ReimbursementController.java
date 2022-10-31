package com.erp.server.workflow.controller.api;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.workflow.dto.*;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Classname ReimbursementController
 * @Description TODO
 * @Date 2022-08-10 14:08
 * @Created by yl
 */
@RestController
@RequestMapping("workflow")
@Slf4j
public class ReimbursementController extends BaseController {


    @Autowired
    private WorkflowService workflowService;


    @Autowired
    private ProcessTaskService processTaskService;

    //部署流程
    @PostMapping("/deploy")
    public ApiResult deploy(@RequestBody @Validated DeployProcessDTO dto) {
        workflowService.deployDefinitionByResource(dto);
        return success();
    }


    //启动流程
    @PostMapping("/startProcess")
    public ApiResult startProcess(@RequestBody @Validated StartProcessDTO dto) {
        workflowService.startProcess(dto);
        return success();
    }

    //查看任务
    @GetMapping("/queryMyToDo")
    public ApiResult queryMyToDo(String userId) {
        List<TaskShowDTO> list = processTaskService.queryMyToDo(userId);
        return success(list);
    }

    //审批通过任务
    @PostMapping("/taskPass")
    public ApiResult taskPass(@RequestBody @Validated ApproveProcessDTO dto) {
       processTaskService.taskPass(dto);
       return success();
    }


    // 删除任务 不一定删除成功
    @PostMapping("/removeTask")
    public ApiResult removeTask(String taskId) {
        processTaskService.removeTask(taskId);
        return success();
    }


    //驳回到上一级
    @PostMapping("/rejectGoBack")
    public ApiResult rejectGoBack(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.rejectGoBackProcess(dto);
        return success();
    }

    //驳回到起点
    @PostMapping("/rejectOrigin")
    public ApiResult rejectOrigin(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.rejectOriginProcess(dto);
        return success();
    }

    //撤销流程
    @PostMapping("/withDraw")
    public ApiResult withDraw(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.withDrawProcess(dto);
        return success();
    }

    //撤回流程
    @PostMapping("/fetchBack")
    public ApiResult fetchBackProcess(@RequestBody @Validated ApproveProcessDTO dto) {
        workflowService.fetchBackProcess(dto);
        return success();
    }

    //任务历史
    @GetMapping("/queryMyTaskHistory")
    public ApiResult queryMyTaskHistory(QueryProcessDTO dto) {
        List<HistoricTaskInstance> list=processTaskService.historicTaskInstances(dto);
        return success(list);
    }

    //单个任务
    @GetMapping("/taskInfo")
    public ApiResult queryTaskInfo(@RequestBody @Validated ApproveProcessDTO dto) {
        TaskShowDTO vo=processTaskService.queryTaskInfo(dto);
        return success(vo);
    }

    //查看流程审批情况
    @GetMapping("/queryProcessApprove")
    public ApiResult queryProcessApprove(@RequestBody @Validated ProcessBaseDTO dto) {
        List<ApproveRecordShowDTO> resultList=workflowService.queryApproveRecord(dto);
        return success(resultList);
    }

}
