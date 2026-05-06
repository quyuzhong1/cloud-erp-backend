package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.wms.entity.WarehouseLocationMappingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 第三方仓位映射Feign
 * @date 2024-08-14
 * @author tanmujin
 */
@FeignClient(name = "erp-wms", contextId = "warehouseLocationMapping", configuration = {FeignErrorDecoder.class})
public interface WarehouseLocationMappingFeign {

    default List<WarehouseLocationMappingEntity> listBySysWarehouseId(String sysWarehouseId) {
        return listBySysWarehouseId(sysWarehouseId, PlatformDictEnum.WDT.getCode());
    }

    @GetMapping(value = "/listBySysWarehouseId")
    List<WarehouseLocationMappingEntity> listBySysWarehouseId(@RequestParam String sysWarehouseId,
                                                              @RequestParam(required = false) String dictPlatform);
}
