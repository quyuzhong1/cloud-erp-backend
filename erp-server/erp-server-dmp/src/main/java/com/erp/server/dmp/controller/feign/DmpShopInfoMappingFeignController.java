package com.erp.server.dmp.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.dmp.entity.ShopInfoMappingEntity;
import com.erp.server.dmp.service.ShopInfoMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: wtr
 * @Date: 2026/2/10 15:58
 * @Param:
 * @Return:
 * @Description:
 **/
@Slf4j
@RestController
@RequestMapping("/feign/dmp")
public class DmpShopInfoMappingFeignController extends BaseController {

    @Resource
    private ShopInfoMappingService shopInfoMappingService;

    @PostMapping("/getByShopIdAndType")
    public ShopInfoMappingEntity getByShopIdAndType(@RequestBody String shopId, String platformName){
        return shopInfoMappingService.getByShopIdAndType(shopId, platformName);
    }
}
