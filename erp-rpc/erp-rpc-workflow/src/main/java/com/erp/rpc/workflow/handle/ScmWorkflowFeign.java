package com.erp.rpc.workflow.handle;

import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @description: scm服务结束审核feign
 * @author Will
 * @date: 2023/7/3 15:27
 */
@FeignClient(value = "erp-scm", contextId = "workflow-scm")
public interface ScmWorkflowFeign extends BaseWorkflowService{

    /**
     * 结束审核
     * @param dto
     * @return
     */
    @PostMapping("/feign/scmWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);

}

