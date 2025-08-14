package com.erp.rpc.plm.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-plm" , contextId = "plmBaseDataFeign",configuration = {FeignErrorDecoder.class})
public interface PlmBaseDataFeign extends BaseDataFeign{

}
