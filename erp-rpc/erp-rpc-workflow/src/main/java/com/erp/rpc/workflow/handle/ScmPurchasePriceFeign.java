package com.erp.rpc.workflow.handle;

import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @description: 采购价目审核通过feign
 * @author Will
 * @date: 2023/7/3 15:27
 */
@FeignClient(value = "erp-scm", contextId = "purchasePrice")
public interface ScmPurchasePriceFeign extends BaseWorkflowService{

    /**
     * purchase_price 采购价目
     * @param dto
     * @return
     */
    @PostMapping("/feign/scmWorkflow/approveEnd")
    Boolean approveEnd(EndProcessDTO dto);

}

