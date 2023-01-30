package com.erp.server.workflow.controller.feign;

import com.erp.common.controller.BaseController;
import com.erp.model.workflow.dto.*;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Classname ProcessFeignController
 * @Description TODO
 * @Date 2022-10-17 10:34
 * @Created by yl
 */

@RestController
@RequestMapping("workflow/feign/process")
@Slf4j
public class ProcessFeignController extends BaseController {

    @Autowired
    public WorkflowService workflowService;

    @Autowired
    private ProcessTaskService processTaskService;


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

    //审核通过任务
    @PostMapping("/taskPass")
    public ProcessNodeDTO taskPass(@RequestBody @Validated ApproveProcessDTO dto) {
        ProcessNodeDTO node = processTaskService.taskPass(dto);
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

    //根据审核任务id查看任务
    @PostMapping("/queryMyToDoByTaskId")
    public List<TaskShowDTO> queryMyToDoByTaskId(String processId) {
        List<TaskShowDTO> list = processTaskService.queryMyToDoByTaskId(processId);
        return list;
    }

    //查看流程审批情况
    @PostMapping("/queryProcessApprove")
    public List<ApproveRecordShowDTO> queryProcessApprove(@RequestBody ProcessBaseDTO dto) {
        List<ApproveRecordShowDTO> resultList = workflowService.queryApproveRecord(dto);
        return resultList;
    }
}
