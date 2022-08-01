package com.cloud.erp.admin.modules.feign;


import com.erp.common.dto.base.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Classname SysAuthFeign
 * @Description TODO
 * @Date 2022-07-18 15:53
 * @Created by yl
 */
@FeignClient("erp-auth")
public interface SysAuthFeign {

    //设置登录ip账号登录
    @PostMapping("auth/feign/user/logout")
    ApiResult logout(String  accessToken);
}
