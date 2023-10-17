package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.oms.service.ShopInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/feign/shop")
public class ShopInfoFeignController extends BaseController {

    @Resource
    private ShopInfoService shopInfoService;

    /**
     * 获取店铺列表
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<ShopInfoEntity>> list() {
        List<ShopInfoEntity> list = shopInfoService.list();
        return success(list);
    }

    /**
     * 更新店铺信息
     *
     * @param shopInfoEntity
     * @return
     */
    @PostMapping("/updateShopInfoById")
    Boolean updateShopInfoById(@RequestBody ShopInfoEntity shopInfoEntity){
        return shopInfoService.updateShopInfoById(shopInfoEntity);
    }

    /**
     * 通过ID查询店铺信息
     */
    @GetMapping("/getShopInfoById")
    public ShopInfoEntity getShopInfoById(@RequestParam String id) {
        return shopInfoService.getById(id);
    }
}
