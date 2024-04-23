package com.erp.rpc.workflow.handle;

import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @description: plm服务结束审核feign
 * @author Will
 * @date: 2023/7/3 15:27
 */
@FeignClient(value = "erp-plm", contextId = "workflow-plm")
public interface PlmWorkflowFeign extends BaseWorkflowService{

    /**
     * 结束审核
     * @param dto
     * @return
     */
    @PostMapping("/feign/plmWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);

}

