package com.erp.rpc.wms.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.common.business.feign.BaseDataFeign;

@FeignClient(name = "erp-scm" , contextId = "scmBaseDataFeign")
public interface ScmBaseDataFeign extends BaseDataFeign{

}
