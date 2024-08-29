package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "erp-wms", contextId = "overseasProviderFeign")
public interface OverseasProviderFeign {
    /**
     * 根据平台编号查询平台信息
     * @Author Luo_WG
     * @Date 2024/1/18 14:35
     * @param code
     * @return com.erp.model.wms.entity.OverseasProviderEntity
     **/
    @GetMapping("/feign/overseasProvider/getByWarehouseId")
    OverseasProviderEntity getByWarehouseId(@RequestParam("warehouseId") String warehouseId);
    /**
     * 查询仓库信息
     *
     * @param feignDTO
     * @return com.erp.model.wms.entity.OverseasProviderEntity
     **/
    @PostMapping("/feign/overseasProvider/getOverseasWarehouse")
    OverseasProviderDTO.FeignDTO getOverseasWarehouse(@RequestBody OverseasProviderDTO.FeignDTO feignDTO);
    @PostMapping("/feign/overseasProvider/feignBind")
    void feignBind(@RequestBody OverseasProviderDTO.FeignDTO feignDTO);
}
