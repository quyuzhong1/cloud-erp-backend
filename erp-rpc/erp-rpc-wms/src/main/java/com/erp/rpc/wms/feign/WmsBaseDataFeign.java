package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-wms" , contextId = "wmsBaseDataFeign",configuration = {FeignErrorDecoder.class})
public interface WmsBaseDataFeign extends BaseDataFeign{

}
