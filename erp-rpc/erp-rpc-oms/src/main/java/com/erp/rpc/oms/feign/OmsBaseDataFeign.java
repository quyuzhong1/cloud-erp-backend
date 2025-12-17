package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-oms" , contextId = "omsBaseDataFeign",configuration = {FeignErrorDecoder.class})
public interface OmsBaseDataFeign extends BaseDataFeign{

}
