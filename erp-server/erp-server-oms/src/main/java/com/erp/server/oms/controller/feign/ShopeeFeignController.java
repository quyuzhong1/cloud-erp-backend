package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.oms.service.ShopAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 虾皮授权信息记录
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Slf4j
@RestController
@RequestMapping("/feign/shopee")
public class ShopeeFeignController extends BaseController {

    @Resource
    private ShopAuthService shopAuthService;


    /**
     * 获取店铺列表
     *
     * @return
     */
    @GetMapping("/getShopeeShopList")
    public ApiResult<List<ShopAuthEntity>> getShopeeShopList(@RequestParam(value = "type") String type,@RequestParam(value = "status") String status) {
        return success(shopAuthService.getShopeeShopList(type,status));
    }

    /**
     * 获取商铺详情
     *
     * @return
     */
    @GetMapping("/getShopeeShopById")
    public ApiResult<ShopAuthEntity> getShopeeShopById(@RequestParam(value = "shopId") String shopId) {
        return success(shopAuthService.getByShopId(shopId));
    }

    /**
     * 更新商铺token
     *
     * @return
     */
    @PostMapping("/updateShopeeToken")
    public ApiResult updateShopeeToken(@RequestBody ShopAuthEntity shopAuthEntity) {
        shopAuthService.updateShopeeToken(shopAuthEntity);
        return success();
    }

}
