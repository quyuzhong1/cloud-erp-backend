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
}
