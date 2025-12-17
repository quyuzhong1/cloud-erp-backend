package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-sys" , contextId = "sysBaseDataFeign",configuration = {FeignErrorDecoder.class})
public interface SysBaseDataFeign extends BaseDataFeign{

}
