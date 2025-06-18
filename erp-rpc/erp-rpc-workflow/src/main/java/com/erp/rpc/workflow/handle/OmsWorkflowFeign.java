package com.erp.rpc.workflow.handle;

import com.common.business.dto.ApproveDTO;
import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @description: oms服务结束审核feign
 * @author Will
 * @date: 2023/7/3 15:27
 */
@FeignClient(value = "erp-oms", contextId = "workflow-oms")
public interface OmsWorkflowFeign extends BaseWorkflowService{

    /**
     * 结束审核
     * @param dto
     * @return
     */
    @PostMapping("/feign/omsWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);


    /**
     * 反审核
     * @author will
     * @date 2025/6/18 09:25
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/omsWorkflow/disApprove")
    Boolean disApprove(ApproveDTO.DisApproveDTO dto);

    /**
     * 撤销流程
     * @author will
     * @date 2025/6/18 10:40
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/omsWorkflow/cancelProcess")
    Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto);

}

