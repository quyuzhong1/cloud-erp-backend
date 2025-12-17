package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-tms" , contextId = "tmsBaseDataFeign" ,configuration = {FeignErrorDecoder.class})
public interface TmsBaseDataFeign extends BaseDataFeign{

}
