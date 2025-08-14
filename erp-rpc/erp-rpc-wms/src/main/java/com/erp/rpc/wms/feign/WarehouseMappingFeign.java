package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.WarehouseMappingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 仓库映射表
 */
@FeignClient(name = "erp-wms", contextId = "warehouseMapping",configuration = {FeignErrorDecoder.class})
public interface WarehouseMappingFeign {

    /**
     * 根据仓库id查询映射信息
     * @Author Luo_WG
     * @Date 2024/1/30 18:33
     * @param warehouseIdList
     * @return java.util.List<com.erp.model.wms.dto.WarehouseMappingDTO.MappingViewDTO>
     **/
    @PostMapping("/feign/warehouseMapping/listMappingViewByIds")
    List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByWarehouseIds(@RequestBody List<String> warehouseIdList);

    /**
     * 根据平台编码查询仓库映射信息
     * @Author Luo_WG
     * @Date 2024/1/31 12:32
     * @param dictPlatform
     * @return java.util.List<com.erp.model.wms.dto.WarehouseMappingDTO.MappingViewDTO>
     **/
    @PostMapping("/feign/warehouseMapping/listMappingViewByDictPlatform")
    List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByDictPlatform(@RequestBody String dictPlatform);
}


