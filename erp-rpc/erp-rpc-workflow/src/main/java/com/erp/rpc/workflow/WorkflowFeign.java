package com.erp.rpc.workflow;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Classname WorkflowFeign
 * @Description TODO
 * @Date 2022-10-18 17:00
 * @Created by yl
 */
@FeignClient("erp-workflow")
public interface WorkflowFeign {
}
