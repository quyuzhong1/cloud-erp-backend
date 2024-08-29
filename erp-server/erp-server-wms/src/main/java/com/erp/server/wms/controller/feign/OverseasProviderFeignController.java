package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import org.apache.xpath.operations.Bool;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 海外仓feign
 */
@RestController
@RequestMapping("/feign/overseasProvider")
public class OverseasProviderFeignController {

    @Resource
    private OverseasProviderService overseasProviderService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;


    /**
     * 根据平台编号查询平台信息
     * @Author Luo_WG
     * @Date 2024/1/18 14:35
     * @param code
     * @return com.erp.model.wms.entity.OverseasProviderEntity
     **/
    @GetMapping("/getByWarehouseId")
    public OverseasProviderEntity getByWarehouseId(@RequestParam("warehouseId") String warehouseId) {
        return overseasProviderService.getByWarehouseId(warehouseId);
    }

    /**
     * 查询仓库信息
     *
     * @param feignDTO
     * @return com.erp.model.wms.entity.OverseasProviderEntity
     **/
    @PostMapping("/getOverseasWarehouse")
    public OverseasProviderDTO.FeignDTO getOverseasWarehouse(@RequestBody OverseasProviderDTO.FeignDTO feignDTO){
        return overseasProviderService.getOverseasWarehouse(feignDTO);
    }
    /**
     * 查询仓库信息
     *
     * @param feignDTO
     * @return com.erp.model.wms.entity.OverseasProviderEntity
     **/
    @PostMapping("/feignBind")
    public Boolean feignBind(@RequestBody OverseasProviderDTO.FeignDTO feignDTO){
        return overseasProviderWarehouseService.feignBind(feignDTO);
    }
}
