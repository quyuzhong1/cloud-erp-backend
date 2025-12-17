package com.erp.rpc.workflow.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-workflow" , contextId = "workflowBaseDataFeign",configuration = {FeignErrorDecoder.class})
public interface WorkflowBaseDataFeign extends BaseDataFeign{

}
