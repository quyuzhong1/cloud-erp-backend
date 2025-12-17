package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 
 * @date 2024-08-15
 * @author tanmujin
 */
@FeignClient(name = "erp-wms", contextId = "wdtPushFeign",configuration = {FeignErrorDecoder.class})
public interface WdtPushFeign {
}
