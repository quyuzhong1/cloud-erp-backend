package com.erp.rpc.plm.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-plm" , contextId = "plmBaseDataFeign")
public interface PlmBaseDataFeign extends BaseDataFeign{

}
