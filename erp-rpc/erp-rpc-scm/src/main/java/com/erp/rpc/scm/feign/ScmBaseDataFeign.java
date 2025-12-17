package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-scm" , contextId = "scmBaseDataFeign",configuration = {FeignErrorDecoder.class})
public interface ScmBaseDataFeign extends BaseDataFeign{

}
