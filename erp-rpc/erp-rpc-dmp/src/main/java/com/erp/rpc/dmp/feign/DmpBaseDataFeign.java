package com.erp.rpc.dmp.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-dmp" , contextId = "dmpBaseDataFeign")
public interface DmpBaseDataFeign extends BaseDataFeign{

}
