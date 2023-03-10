package com.erp.rpc.dmp.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @description: DMP远程调用接口
 * @author Will
 * @date: 2023/1/12 16:54
 */
@FeignClient("erp-dmp")
public interface DmpTaskFeign {


}