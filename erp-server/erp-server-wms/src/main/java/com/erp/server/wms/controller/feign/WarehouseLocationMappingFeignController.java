package com.erp.server.wms.controller.feign;

import com.common.business.enums.PlatformDictEnum;
import com.erp.model.wms.entity.WarehouseLocationMappingEntity;
import com.erp.server.wms.service.WarehouseLocationMappingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 第三方仓位映射Feign接口
 * @date 2024-08-14
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/wdt_warehouse_location_mapping")
public class WarehouseLocationMappingFeignController {

    @Resource
    private WarehouseLocationMappingService warehouseLocationMappingService;

    @GetMapping(value = "/listBySysWarehouseId")
    public List<WarehouseLocationMappingEntity> listBySysWarehouseId(@RequestParam String sysWarehouseId,
                                                                     @RequestParam(required = false) String dictPlatform) {
        String platform = StringUtils.defaultIfBlank(dictPlatform, PlatformDictEnum.WDT.getCode());
        return warehouseLocationMappingService.lambdaQuery()
                .eq(WarehouseLocationMappingEntity::getSysWarehouseId, sysWarehouseId)
                .eq(WarehouseLocationMappingEntity::getDictPlatform, platform)
                .list();
    }
}
