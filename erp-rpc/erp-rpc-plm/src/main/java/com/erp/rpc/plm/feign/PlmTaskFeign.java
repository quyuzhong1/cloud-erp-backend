package com.erp.rpc.plm.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * plm 远程调用接口
 * @Classname PlmTaskFeign
 * @Description TODO
 * @Date 2022-10-21 9:06
 * @Created by yl
 */
@FeignClient("erp-plm")
public interface PlmTaskFeign {


}
