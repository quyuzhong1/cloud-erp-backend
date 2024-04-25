package com.erp.rpc.sys.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-sys" , contextId = "sysBaseDataFeign")
public interface SysBaseDataFeign extends BaseDataFeign{

}
