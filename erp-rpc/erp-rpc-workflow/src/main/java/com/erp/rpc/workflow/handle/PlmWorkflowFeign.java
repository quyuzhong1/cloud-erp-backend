package com.erp.rpc.workflow.handle;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.ApproveDTO;
import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

/**
 * @description: plm服务结束审核feign
 * @author Will
 * @date: 2023/7/3 15:27
 */
@FeignClient(value = "erp-plm", contextId = "workflow-plm",configuration = {FeignErrorDecoder.class})
public interface PlmWorkflowFeign extends BaseWorkflowService{

    /**
     * 结束审核
     * @param dto
     * @return
     */
    @PostMapping("/feign/plmWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);

    /**
     * @description: 获取流程的单据头和明细的数据
     * @author jack
     * @date: 2025-05-22
     * @return Map<String, Object>
     */
    @PostMapping("/feign/plmWorkflow/getVariablesMap")
    Map<String, Object> getVariablesMap(EndProcessDTO dto);

    /**
     * 反审核
     * @author will
     * @date 2025/6/18 09:24
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/plmWorkflow/disApprove")
    Boolean disApprove(ApproveDTO.DisApproveDTO dto);
    /**
     * 撤销流程
     * @author will
     * @date 2025/6/18 10:39
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/plmWorkflow/cancelProcess")
    Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto);

}

