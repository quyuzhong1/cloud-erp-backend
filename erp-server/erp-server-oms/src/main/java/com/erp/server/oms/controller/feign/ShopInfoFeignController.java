package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.oms.service.ShopAuthService;
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
    @Resource
    private ShopAuthService shopAuthService;
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
     * 获取店铺授权列表
     *
     * @return
     */
    @PostMapping("/getAuthShopByPlatformType")
    public ApiResult<List<ShopAuthEntity>> getAuthShopByPlatformType(@RequestParam("platformType") String platformType) {
        List<ShopAuthEntity> list = shopAuthService.getAuthShopByPlatformType(platformType);
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

    /**
     * 根据店铺id查询店铺信息
     * @Author Luo_WG
     * @Date 2023/11/1 12:09
     * @param ids
     * @return com.erp.model.oms.entity.ShopInfoEntity
     **/
    @PostMapping("/listShopInfoByIds")
    public List<ShopInfoEntity> listShopInfoByIds(@RequestBody List<String> ids) {
        return shopInfoService.listByIds(ids);
    }

    /**
     * 根据仓库id查询店铺
     * @Author Luo_WG
     * @Date 2023/11/23 16:03
     * @param warehouseIds
     * @return java.util.List<com.erp.model.oms.entity.ShopInfoEntity>
     **/
    @PostMapping("/listShopInfoByWarehouseIds")
    public List<ShopInfoEntity> listShopInfoByWarehouseIds(@RequestBody List<String> warehouseIds) {
        return shopInfoService.listShopInfoByWarehouseIds(warehouseIds);
    }

    /**
     * 获取授权信息
     * @param shopId
     * @return
     */
    @PostMapping("/getShopAuthByShopId")
    public ShopAuthEntity getShopAuth(@RequestBody String shopId) {
        return shopAuthService.getByShopId(shopId);
    }
}
