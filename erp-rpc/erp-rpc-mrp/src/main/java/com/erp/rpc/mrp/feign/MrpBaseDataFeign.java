package com.erp.rpc.mrp.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.feign.BaseDataFeign;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "erp-mrp" , contextId = "mrpBaseDataFeign",configuration = {FeignErrorDecoder.class})
public interface MrpBaseDataFeign extends BaseDataFeign{

}
