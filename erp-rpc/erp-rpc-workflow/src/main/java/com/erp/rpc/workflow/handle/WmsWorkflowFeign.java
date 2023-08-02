package com.erp.rpc.workflow.handle;

import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @description: wms服务结束审核feign
 * @author Will
 * @date: 2023/8/2 15:27
 */
@FeignClient(value = "erp-wms", contextId = "workflow-wms")
public interface WmsWorkflowFeign extends BaseWorkflowService{

    /**
     * 结束审核
     * @param dto
     * @return
     */
    @PostMapping("/feign/wmsWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);

}

