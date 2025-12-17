package com.erp.rpc.auth.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Classname AuthFeign

 * @Date 2022-08-19 11:58
 * @Created by yl
 */
@FeignClient(name = "erp-auth", contextId = "authFeign",configuration = {FeignErrorDecoder.class})
public interface AuthFeign {

    //设置登录ip账号登录
    @PostMapping("feign/user/logout")
    ApiResult logout(String  accessToken);
}
