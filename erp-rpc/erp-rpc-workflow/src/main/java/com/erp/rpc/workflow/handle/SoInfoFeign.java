package com.erp.rpc.workflow.handle;

import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 销售订单feign
 * @Author Luo_WG
 * @Date 2023/7/4 11:18
 **/
@FeignClient(value = "erp-oms", contextId = "so")
public interface SoInfoFeign extends BaseWorkflowService {
    /**
     * purchase_price 采购价目
     * @param dto
     * @return
     */
    @PostMapping("/feign/omsWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);
}
