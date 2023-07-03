package com.erp.server.scm.controller.feign;

import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.scm.service.WorkflowProcessService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: 工作流feign
 * @author Will
 * @date: 2023/7/3 15:32
 */
@RestController
@RequestMapping("feign/scmWorkflow")
public class ScmWorkflowFeignController {

    @Resource
    private WorkflowProcessService workflowProcessService;

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:42
     * @param dto
     * @return Boolean
     */
    @PostMapping("/approveEnd")
    public Boolean approveEnd(@RequestBody EndProcessDTO dto) {
      return workflowProcessService.approveEnd(dto);
    }
}
