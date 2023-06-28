package com.erp.rpc.workflow.handle;

import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Author Cloud
 * @Date 2023/6/27 18:29
 **/
@FeignClient(value = "erp-oms", contextId = "omsCustomerInfoFeign")
public interface OmsCustomerInfoFeign extends BaseWorkflowService{

    /**
     * customer_info 客户列表
     * @param dto
     * @return
     */
    @PostMapping("/feign/omsWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);

}

