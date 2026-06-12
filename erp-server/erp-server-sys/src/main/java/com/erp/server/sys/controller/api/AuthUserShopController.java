package com.erp.server.sys.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.AuthUserShopDTO;
import com.erp.server.sys.service.AuthUserShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户-店铺权限
 *
 * @author zdy
 * @since 2025-02-27
 */
@Slf4j
@RestController
@LogSystemModule("用户-店铺权限")
@RequestMapping("/authUserShop")
public class AuthUserShopController extends BaseController {

    @Resource
    private AuthUserShopService authUserShopService;


    /**
     * 查询平台下登陆的用户店铺权限
     * @author will
     * @date:  2025-02-27
     * @param paramDTO
     * @return ApiResult<List<AuthUserShopDTO.ShopAuthListDTO>>
     */
    @PostMapping("/listAuthShop")
    public ApiResult<List<AuthUserShopDTO.ShopAuthListDTO>> listAuthShop(@RequestBody @Validated AuthUserShopDTO.ShopAuthParamDTO paramDTO){
        return success(authUserShopService.listAuthShop(paramDTO));
    }
}
