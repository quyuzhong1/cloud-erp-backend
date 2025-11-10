package com.erp.rpc.workflow.handle;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.ApproveDTO;
import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @description: fms服务结束审核feign
 * @author System
 * @date: 2025/11/10
 */
@FeignClient(value = "erp-fms", contextId = "workflow-fms", configuration = {FeignErrorDecoder.class})
public interface FmsWorkflowFeign extends BaseWorkflowService{

    /**
     * 结束审核
     * @param dto
     * @return
     */
    @PostMapping("/feign/fmsWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);

    /**
     * 反审核
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/fmsWorkflow/disApprove")
    Boolean disApprove(ApproveDTO.DisApproveDTO dto);

    /**
     * 撤销流程
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/fmsWorkflow/cancelProcess")
    Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto);

}
