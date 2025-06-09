package com.erp.rpc.mrp.feign;

import com.common.business.feign.BaseDataFeign;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "erp-mrp" , contextId = "mrpBaseDataFeign")
public interface MrpBaseDataFeign extends BaseDataFeign{

}
