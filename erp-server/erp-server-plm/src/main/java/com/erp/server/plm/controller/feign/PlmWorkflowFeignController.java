package com.erp.server.plm.controller.feign;

import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.plm.service.WorkflowProcessService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: 工作流feign
 * @author Will
 * @date: 2023/8/2 16:32
 */
@RestController
@RequestMapping("feign/plmWorkflow")
public class PlmWorkflowFeignController {

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
