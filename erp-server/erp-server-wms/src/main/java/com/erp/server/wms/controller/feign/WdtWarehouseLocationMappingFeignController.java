package com.erp.server.wms.controller.feign;

import com.erp.model.wms.entity.WdtWarehouseLocationMappingEntity;
import com.erp.server.wms.service.WdtWarehouseLocationMappingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 旺店通仓位映射Feign接口
 * @date 2024-08-14
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/wdt_warehouse_location_mapping")
public class WdtWarehouseLocationMappingFeignController {

    @Resource
    private WdtWarehouseLocationMappingService wdtWarehouseLocationMappingService;

    @GetMapping(value = "/listBySysWarehouseId")
    public List<WdtWarehouseLocationMappingEntity> listBySysWarehouseId(@RequestParam String sysWarehouseId) {
        return wdtWarehouseLocationMappingService.lambdaQuery().eq(WdtWarehouseLocationMappingEntity::getSysWarehouseId, sysWarehouseId).list();
    }
}
