package com.cloud.erp.workflow.controller;

import com.alibaba.fastjson2.JSONObject;
import com.cloud.erp.workflow.dto.*;
import com.cloud.erp.workflow.service.ProcessDefinitionService;
import com.cloud.erp.workflow.service.ProcessInstanceService;
import com.cloud.erp.workflow.service.ProcessTaskService;
import com.cloud.erp.workflow.vo.TaskVO;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname ReimbursementController
 * @Description TODO
 * @Date 2022-08-10 14:08
 * @Created by yl
 */
@RestController
@RequestMapping("workflow")
public class ReimbursementController extends BaseController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private ProcessTaskService processTaskService;

    //部署流程
    @PostMapping("/deploy")
    public ApiResult deploy(@RequestBody @Validated DeployDTO dto) {
        processDefinitionService.deployDefinitionByResource(dto.getName(), dto.getResource());
        return success();
    }


    //启动流程
    @PostMapping("/startProcess")
    public ApiResult startProcess(@RequestBody @Validated StartProcessDTO dto) {
        processInstanceService.startProcessInstanceByKey(dto);
        return success();
    }

    //查看任务
    @GetMapping("/queryMyToDo")
    public ApiResult queryMyToDo(String userId) {
        List<TaskVO> list = processTaskService.queryMyToDo(userId);
        return success(list);
    }

    //审批通过任务
    @PostMapping("/taskPass")
    public ApiResult taskPass(@RequestBody @Validated ApproveProcessPassDTO dto) {
       processTaskService.taskPass(dto);
       return success();
    }


    // 删除任务 不一定删除成功
    @PostMapping("/removeTask")
    public ApiResult removeTask(String taskId) {
        processTaskService.removeTask(taskId);
        return success();
    }


    //驳回
    @PostMapping("/reject")
    public ApiResult reject(@RequestBody @Validated ApproveProcessRejectDTO dto) {
        processInstanceService.rejectBack(dto);
        return success();
    }

    //任务历史
    @GetMapping("/queryMyTaskHistory")
    public ApiResult queryMyTaskHistory(String userId) {
        List<HistoricTaskInstance> list=processTaskService.historicTaskInstances(userId);
        return success(list);
    }

    //单个任务
    @GetMapping("/taskInfo")
    public ApiResult queryTaskInfo(@RequestBody @Validated ProcessBaseDTO  dto) {
        TaskVO vo=processTaskService.queryTaskInfo(dto);
        return success(vo);
    }



}
