package com.erp.rpc.oms.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-oms" , contextId = "omsBaseDataFeign")
public interface OmsBaseDataFeign extends BaseDataFeign{

}
