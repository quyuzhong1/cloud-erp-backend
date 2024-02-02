package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.server.oms.service.ShopSysUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public List<ShopSysUserAuthDTO.ViewDTO> listShopSysUserAuthByUserIdList(@RequestBody List<String> userIdList) {
        List<ShopSysUserAuthDTO.ViewDTO> list = shopSysUserAuthService.listShopSysUserAuthByUserIdList(userIdList);
        return list;
    }

    /**
     * 根据店铺id查询已关联用户id
     *
     * @return
     */
    @PostMapping("/listUserIdByShopIdList")
    public List<String> listUserIdByShopIdList(@RequestBody List<String> shopIdList) {
        List<String> list = shopSysUserAuthService.listUserIdByShopIdList(shopIdList);
        return list;
    }

    /**
     * 下拉用户拥有权限的店铺
     * @Author Luo_WG
     * @Date 2024/1/31 11:11
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.oms.dto.ShopSysUserAuthDTO.ViewShopDTO>>
     **/
    @PostMapping("/listUserAuthShop")
    public List<ShopSysUserAuthDTO.ViewShopDTO> listUserAuthShop(@RequestBody ShopSysUserAuthDTO.UserAuthShopParamDTO dto) {
        List<ShopSysUserAuthDTO.ViewShopDTO> viewShopDTOList = shopSysUserAuthService.listUserAuthShop(dto);
        return viewShopDTOList;
    }
}
