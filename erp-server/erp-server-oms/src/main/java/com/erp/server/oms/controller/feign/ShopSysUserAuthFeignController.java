package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.server.oms.service.ShopSysUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 店铺管理
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Slf4j
@RestController
@RequestMapping("/feign/shopSysUserAuth")
public class ShopSysUserAuthFeignController extends BaseController {

    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;

    /**
     * 根据用户id查询店铺权限信息
     *
     * @return
     */
    @PostMapping("/listShopSysUserAuthByUserIdList")
    public ApiResult<List<ShopSysUserAuthDTO.ViewDTO>> listShopSysUserAuthByUserIdList(List<String> userIdList) {
        List<ShopSysUserAuthDTO.ViewDTO> list = shopSysUserAuthService.listShopSysUserAuthByUserIdList(userIdList);
        return success(list);
    }

}
