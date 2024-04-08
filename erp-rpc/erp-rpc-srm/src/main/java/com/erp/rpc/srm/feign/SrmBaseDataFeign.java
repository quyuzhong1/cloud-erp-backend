package com.erp.rpc.srm.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-srm" , contextId = "srmBaseDataFeign")
public interface SrmBaseDataFeign extends BaseDataFeign{

}
