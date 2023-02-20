package com.erp.server.auth.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.auth.server.AuthTokenService;
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
