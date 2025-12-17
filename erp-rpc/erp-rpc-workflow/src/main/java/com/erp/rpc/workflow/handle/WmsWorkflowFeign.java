package com.erp.rpc.workflow.handle;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.config.FeignTimeoutConfig;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @description: wms服务结束审核feign
 * @author Will
 * @date: 2023/8/2 15:27
 */
@FeignClient(value = "erp-wms", contextId = "workflow-wms",configuration = {FeignErrorDecoder.class, FeignTimeoutConfig.class})
public interface WmsWorkflowFeign extends BaseWorkflowService{
    /**
     * 审核
     * @param dto
     * @return
     */
    @PostMapping("/feign/wmsWorkflow/approve")
    BatchResultDTO approve(ApproveDTO.ApproveOneDTO dto);
    /**
     * 结束审核
     * @param dto
     * @return
     */
    @PostMapping("/feign/wmsWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);

    /**
     * 反审核
     * @author will
     * @date 2025/6/18 09:25
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/wmsWorkflow/disApprove")
    Boolean disApprove(ApproveDTO.DisApproveDTO dto);

    /**
     * 撤销流程
     * @author will
     * @date 2025/6/18 10:40
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/wmsWorkflow/cancelProcess")
    Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
     * 添加评论
     * @author will
     * @date 2025/6/18 10:40
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/wmsWorkflow/addComment")
    Boolean addComment(ApproveDTO.AddCommentDTO dto);
}

