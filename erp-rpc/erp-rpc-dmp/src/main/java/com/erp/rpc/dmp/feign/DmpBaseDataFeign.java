package com.erp.rpc.dmp.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-dmp" , contextId = "dmpBaseDataFeign",configuration = {FeignErrorDecoder.class})
public interface DmpBaseDataFeign extends BaseDataFeign{

}
