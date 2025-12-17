package com.erp.rpc.workflow;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 流程任务管理Feign
 * @date 2024-09-05
 * @author tanmujin
 */
@FeignClient(name = "erp-workflow", contextId = "processTaskManagement",configuration = {FeignErrorDecoder.class})
public interface ProcessTaskManagementFeign {

    /**
     * 根据业务ID查询审批记录
     */
    @GetMapping("/feign/processTaskManagement/listApproveHistory")
    List<ProcessTaskManagementDTO.ApproveHistoryDTO> listApproveHistory(@RequestParam String businessId);
}
