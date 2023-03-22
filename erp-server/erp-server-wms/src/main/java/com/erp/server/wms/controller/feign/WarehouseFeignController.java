package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/17 16:01
 */
@RestController
@RequestMapping("wms/feign/warehouse")
public class WarehouseFeignController {

    @Resource
    private WarehouseService  warehouseService;

    @PostMapping("/listWarehouseByIds")
    public List<WarehouseDTO.UpdateDTO> listWarehouseByIds(@RequestBody List<String> ids) {
        return warehouseService.listWarehouseByIds(ids);
    }

    /**
     * 查询所有审核通过并启用的仓库
     * @author Will
     * @date: 2023/3/21 14:26
     * @return List<WarehouseDTO>
     */
    @GetMapping("/listApproveWarehouse")
    public List<WarehouseDTO.UpdateDTO> listApproveWarehouse() {
        return warehouseService.listApproveWarehouse();
    }

}
