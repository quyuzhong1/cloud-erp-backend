package com.erp.rpc.workflow.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-workflow" , contextId = "workflowBaseDataFeign")
public interface WorkflowBaseDataFeign extends BaseDataFeign{

}
