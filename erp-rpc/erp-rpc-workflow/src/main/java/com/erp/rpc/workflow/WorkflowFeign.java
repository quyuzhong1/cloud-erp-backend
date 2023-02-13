package com.erp.rpc.workflow;

import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.model.workflow.vo.ProcessCurrentAuditorVO;
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

    /**
     * 根据用户id 获取用用户所需要的待办的任务列表
     * @author yl
     * @date 2023-01-31 10:59
     * @param userId
     * @return java.util.List<com.erp.model.workflow.vo.MyToDoTaskVO>
     */
    @PostMapping("workflow/feign/process/getMyToDoTasks")
    List<MyToDoTaskVO> getMyToDoTasks(@RequestParam(value="userId") String userId);



    //审核任务通过
    @PostMapping("workflow/feign/process/taskPass")
    ProcessNodeDTO taskPass(@RequestBody ApproveProcessDTO dto);


    //审核任务不通过
    @PostMapping("workflow/feign/process/taskNoPass")
    ProcessNodeDTO taskNoPass(@RequestBody ApproveProcessDTO dto);

    //撤销流程
    @PostMapping("workflow/feign/process/withDraw")
    void withDraw(@RequestBody ApproveProcessDTO dto);

    //取回流程
    @PostMapping("workflow/feign/process/fetchBack")
    void fetchBack(@RequestBody ApproveProcessDTO dto);


    //取回起始点
    @PostMapping("workflow/feign/process/rejectOrigin")
    void rejectOrigin(@RequestBody ApproveProcessDTO dto);

    //终止流程
    @PostMapping("workflow/feign/process/terminate")
    void terminate(@RequestBody ApproveProcessDTO dto);



    //根据审核任务id获取我待办的任务列表
    @PostMapping("workflow/feign/process/queryMyToDoByTaskId")
    List<TaskShowDTO> queryMyToDoByTaskId(@RequestParam(value="taskId") String taskId);

    //获取审核记录
    @PostMapping("workflow/feign/process/getHistoryTaskByProcessId")
    List<ApproveRecordShowDTO> getHistoryTaskByProcessId(@RequestParam(value="processId") String processId);

    /**
     * 获取 业务信息
     * @author yl
     * @date 2023-01-31 15:31
     * @param findProcess
     * @return com.erp.model.workflow.dto.WorkflowBusinessDTO
     */
    @PostMapping("workflow/feign/process/getBusiness")
    BusinessInfoDTO getBusiness(@RequestBody FindProcessDTO findProcess);

    /**
     * 保存 业务流程关系表
     * @author yl
     * @date 2023-01-31 17:13
     * @param businessProcess
     * @return void
     */
    @PostMapping("workflow/feign/process/saveBusinessProcess")
    Boolean saveBusinessProcess(@RequestBody WorkflowBusinessProcessDTO businessProcess);

    @PostMapping("workflow/feign/process/getProcessByBusinessTable")
    MyToDoTaskVO getByBusinessTableId(@RequestBody BusinessTableDTO  dto);

    @PostMapping("workflow/feign/process/getProcess")
    List<WorkflowBusinessProcessDTO> getProcess(@RequestBody List<String> businessTableIds);

    /**
     * 根据业务表获取审核记录
     * @author yl
     * @date 2023-02-11 11:32
     * @param id
     * @return java.util.List<com.erp.model.workflow.dto.ApproveRecordShowDTO>
     */
    @PostMapping("workflow/feign/process/getHistoryTaskByBusinessTableId")
    List<ApproveRecordShowDTO> getHistoryTaskByBusinessTableId(@RequestBody String id);


    /**
     * 根据业务表id获取当前审核人情况
     * @param businessTableIds
     * @return
     */
    @PostMapping("workflow/feign/process/getProcessCurrentAudit")
    List<ProcessCurrentAuditorVO> getProcessCurrentAudit(@RequestBody List<String> businessTableIds);
}
