package com.erp.rpc.fms.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-fms" , contextId = "fmsBaseDataFeign",configuration = {FeignErrorDecoder.class})
public interface FmsBaseDataFeign extends BaseDataFeign{

}

