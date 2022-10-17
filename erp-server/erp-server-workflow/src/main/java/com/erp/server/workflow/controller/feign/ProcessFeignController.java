package com.erp.server.workflow.controller.feign;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.model.workflow.dto.TaskShowDTO;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ProcessNodeDTO startProcess(@RequestBody @Validated StartProcessDTO dto) {
        ProcessNodeDTO process = workflowService.startProcess(dto);
        return process;
    }



    //查看任务
    @PostMapping("/queryMyToDo")
    public ApiResult queryMyToDo(String userId) {
        List<TaskShowDTO> list = processTaskService.queryMyToDo(userId);
        return success(list);
    }
}
