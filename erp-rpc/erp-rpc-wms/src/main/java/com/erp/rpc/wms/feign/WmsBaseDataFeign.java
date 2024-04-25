package com.erp.rpc.wms.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-wms" , contextId = "wmsBaseDataFeign")
public interface WmsBaseDataFeign extends BaseDataFeign{

}
