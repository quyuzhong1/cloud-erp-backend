package com.erp.server.wms.controller.feign;


import com.erp.model.wms.dto.WarehouseMappingDTO;
import com.erp.server.wms.service.WarehouseMappingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * 仓库映射信息
 * @Author Luo_WG
 * @Date 2024/1/30 18:36
 **/
@AllArgsConstructor
@RestController
@RequestMapping(value = "/feign/warehouseMapping")
public class WarehouseMappingFeignController {
    @Resource
    private WarehouseMappingService warehouseMappingService;

    /**
     * 根据仓库id查询映射信息
     * @Author Luo_WG
     * @Date 2024/1/30 18:33
     * @param warehouseIdList
     * @return java.util.List<com.erp.model.wms.dto.WarehouseMappingDTO.MappingViewDTO>
     **/
    @PostMapping("/listMappingViewByIds")
    public List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByWarehouseIds(@RequestBody List<String> warehouseIdList) {
        return warehouseMappingService.listMappingViewByWarehouseIds(warehouseIdList);
    }

    /**
     * 根据平台编码查询仓库映射信息
     * @Author Luo_WG
     * @Date 2024/1/31 12:32
     * @param dictPlatform
     * @return java.util.List<com.erp.model.wms.dto.WarehouseMappingDTO.MappingViewDTO>
     **/
    @PostMapping("/listMappingViewByDictPlatform")
    public List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByDictPlatform(@RequestBody String dictPlatform) {
        return warehouseMappingService.listMappingViewByDictPlatform(dictPlatform);
    }
}
