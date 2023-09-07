package com.erp.rpc.oms.feign;

import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "shopSysUserAuth")
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
}
