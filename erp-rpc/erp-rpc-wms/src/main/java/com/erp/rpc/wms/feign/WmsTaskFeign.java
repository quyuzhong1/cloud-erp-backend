package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.WarehouseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/17 15:56
 */
@FeignClient(name = "erp-wms")
public interface WmsTaskFeign {

    /**
     * 根据userIds查询用户集合
     */
    @PostMapping("wms/feign/warehouse/listWarehouseByIds")
    List<WarehouseDTO> listWarehouseByIds(@RequestBody List<String> warehouseIds);
}
