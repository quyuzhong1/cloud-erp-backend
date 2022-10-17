package com.erp.server.workflow.controller.feign;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.server.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public WorkflowService workflowService;


    //启动流程
    @PostMapping("/startProcess")
    public ApiResult startProcess(@RequestBody @Validated StartProcessDTO dto) {
        workflowService.startProcess(dto);
        return success();
    }
}
