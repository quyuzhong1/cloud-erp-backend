package com.erp.rpc.workflow;

import com.erp.model.workflow.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Classname WorkflowFeign
 * @Description TODO
 * @Date 2022-10-18 17:00
 * @Created by yl
 */
@FeignClient("erp-workflow")
public interface WorkflowFeign {


    //启动流程
    @PostMapping("workflow/feign/process/startProcess")
    ProcessNodeDTO startProcess(@RequestBody StartProcessDTO startProcessDTO);

    //根据人员获取我待办的任务列表
    @PostMapping("workflow/feign/process/queryMyToDo")
    List<TaskShowDTO> queryMyToDo(@RequestParam(value="userId") String userId);

    //审核任务通过
    @PostMapping("workflow/feign/process/taskPass")
    ProcessNodeDTO taskPass(@RequestBody ApproveProcessDTO dto);

    //撤销流程
    @PostMapping("workflow/feign/process/withDraw")
    void withDraw(@RequestBody ApproveProcessDTO dto);

    //取回流程
    @PostMapping("workflow/feign/process/fetchBack")
    void fetchBack(@RequestBody ApproveProcessDTO dto);

    //根据审核任务id获取我待办的任务列表
    @PostMapping("workflow/feign/process/queryMyToDoByTaskId")
    List<TaskShowDTO> queryMyToDoByTaskId(@RequestParam(value="taskId") String taskId);

    //获取审核记录
    @PostMapping("workflow/feign/process/queryProcessApprove")
    List<ApproveRecordShowDTO> queryProcessApprove(@RequestBody ProcessBaseDTO dto);
}
