package com.erp.rpc.tms.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-tms" , contextId = "tmsBaseDataFeign")
public interface TmsBaseDataFeign extends BaseDataFeign{

}
