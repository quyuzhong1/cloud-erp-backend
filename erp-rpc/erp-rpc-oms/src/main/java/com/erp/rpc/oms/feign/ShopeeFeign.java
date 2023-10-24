package com.erp.rpc.oms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.ShopAuthEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "Shopee")
public interface ShopeeFeign {

    /**
     * 获取授权链接
     *
     * @return
     */
    @GetMapping("/feign/shopee/getCodeUrl")
    ApiResult<String> getCodeUrl();

    /**
     * 获取店铺列表
     *
     * @return
     */
    @GetMapping("/feign/shopee/getShopeeShopList")
    ApiResult<List<ShopAuthEntity>> getShopeeShopList(@RequestParam(value = "type") String type);

    /**
     * 获取商铺详情
     *
     * @return
     */
    @GetMapping("/feign/shopee/getShopeeShopById")
    ApiResult<ShopAuthEntity> getShopeeShopById(@RequestParam(value = "shopId") String shopId);

    /**
     * 更新商铺token
     *
     * @return
     */
    @PostMapping("/feign/shopee/updateShopeeToken")
    ApiResult updateShopeeToken(@RequestBody ShopAuthEntity shopAuthEntity);
}
