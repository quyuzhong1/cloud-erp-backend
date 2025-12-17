package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "shopSysUserAuthFeign",configuration = {FeignErrorDecoder.class})
public interface ShopSysUserAuthFeign {
    /**
     * 根据用户id查询店铺权限信息
     * @param userIdList
     * @return
     */
    @PostMapping("feign/shopSysUserAuth/listShopSysUserAuthByUserIdList")
    List<ShopSysUserAuthDTO.ViewDTO> listShopSysUserAuthByUserIdList(@RequestBody List<String> userIdList);

    /**
     * @description: 根据店铺id查询已关联用户id
     * @author Will
     * @date: 2023/9/7 9:35
     * @param shopIdList
     * @return List<String>
     */
    @PostMapping("feign/shopSysUserAuth/listUserIdByShopIdList")
    List<String> listUserIdByShopIdList(@RequestBody List<String> shopIdList);

    /**
     * 下拉用户拥有权限的店铺
     * @Author Luo_WG
     * @Date 2024/1/31 11:11
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.oms.dto.ShopSysUserAuthDTO.ViewShopDTO>>
     **/
    @PostMapping("feign/shopSysUserAuth/listUserAuthShop")
    List<ShopSysUserAuthDTO.ViewShopDTO> listUserAuthShop(@RequestBody ShopSysUserAuthDTO.UserAuthShopParamDTO dto);
}
