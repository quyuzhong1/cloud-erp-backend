package com.erp.rpc.plm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * plm 远程调用接口
 * @Classname PlmTaskFeign
 * @Description TODO
 * @Date 2022-10-21 9:06
 * @Created by yl
 */
@FeignClient("erp-plm")
public interface PlmTaskFeign {

    //获取用户权限
    @PostMapping("plm/product/detail/productDetailProcessPass")
    void productDetailProcessPass(@RequestBody String processId);

}
