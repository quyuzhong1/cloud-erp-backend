package com.erp.rpc.oms.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "ShopInfo")
public interface ShopInfoFeign {

    /**
     * 获取店铺列表
     *
     * @return
     */
    @GetMapping("feign/shop/list")
    ApiResult<List<ShopInfoEntity>> list();

    /**
     * 更新店铺信息
     *
     * @param shopInfoEntity
     * @return
     */
    @PostMapping("feign/shop/updateShopInfoById")
    Boolean updateShopInfoById(@RequestBody ShopInfoEntity shopInfoEntity);
    /**
     * 获取店铺授权列表
     *
     * @param platformType
     * @return
     */
    @PostMapping("feign/shop/getAuthShopByPlatformType")
    ApiResult<List<ShopAuthEntity>> getAuthShopByPlatformType(@RequestParam("platformType") String platformType);

    /**
     * 通过ID查询店铺信息
     */
    @GetMapping("feign/shop/getShopInfoById")
    ShopInfoEntity getShopInfoById(@RequestParam String id);

    /**
     * 根据店铺id查询店铺信息
     * @Author Luo_WG
     * @Date 2023/11/1 12:09
     * @param ids
     * @return com.erp.model.oms.entity.ShopInfoEntity
     **/
    @PostMapping("feign/shop/listShopInfoByIds")
    List<ShopInfoEntity> listShopInfoByIds(@RequestBody List<String> ids);
}
