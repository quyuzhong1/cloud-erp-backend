package com.erp.rpc.wms.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * 
 * @date 2024-08-15
 * @author tanmujin
 */
@FeignClient(name = "erp-wms", contextId = "wdtPushFeign")
public interface WdtPushFeign {
}
