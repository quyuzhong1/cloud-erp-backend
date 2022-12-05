package com.erp.rpc.workflow;

import com.erp.model.workflow.dto.ApproveProcessDTO;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.model.workflow.dto.TaskShowDTO;
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

    //审核任务回退到初始状态
    @PostMapping("workflow/feign/process/rejectOriginProcess")
    ProcessNodeDTO rejectOriginProcess(@RequestBody ApproveProcessDTO dto);

    //根据审核任务id获取我待办的任务列表
    @PostMapping("workflow/feign/process/queryMyToDoByTaskId")
    List<TaskShowDTO> queryMyToDoByTaskId(@RequestParam(value="taskId") String taskId);
}
