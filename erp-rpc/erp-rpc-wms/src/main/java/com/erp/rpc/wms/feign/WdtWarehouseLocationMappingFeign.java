package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.WdtWarehouseLocationMappingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 旺店通仓位映射Feign
 * @date 2024-08-14
 * @author tanmujin
 */
@FeignClient(name = "erp-wms", contextId = "wdtWarehouseLocationMapping")
public interface WdtWarehouseLocationMappingFeign {

    @GetMapping(value = "/listBySysWarehouseId")
    List<WdtWarehouseLocationMappingEntity> listBySysWarehouseId(@RequestParam String sysWarehouseId);
}
