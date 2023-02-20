package com.erp.rpc.auth.feign;

import com.common.core.controller.vo.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Classname AuthFeign
 * @Description TODO
 * @Date 2022-08-19 11:58
 * @Created by yl
 */
@FeignClient("erp-auth")
public interface AuthFeign {

    //设置登录ip账号登录
    @PostMapping("auth/feign/user/logout")
    ApiResult logout(String  accessToken);
}
