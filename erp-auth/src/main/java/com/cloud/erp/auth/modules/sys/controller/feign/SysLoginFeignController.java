package com.cloud.erp.auth.modules.sys.controller.feign;

import com.cloud.erp.auth.modules.web.server.AuthTokenService;
import com.cloud.erp.common.common.ApiResult;
import com.cloud.erp.common.common.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname SysLoginFeignController
 * @Description TODO
 * @Date 2022-07-18 15:43
 * @Created by yl
 */
@RestController
@RequestMapping("auth/feign/user")
public class SysLoginFeignController extends BaseController {

    @Autowired
    private AuthTokenService authTokenService;

    @PostMapping("/logout")
    public ApiResult removeToken(@RequestBody String accessToken) {
        authTokenService.removeToken(accessToken);
        return success();

    }
}
