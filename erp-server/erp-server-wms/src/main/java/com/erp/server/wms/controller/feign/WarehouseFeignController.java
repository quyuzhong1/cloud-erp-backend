package com.erp.server.wms.controller.feign;

import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<WarehouseDTO> listWarehouseByIds(@RequestBody List<String> ids) {
        return warehouseService.listWarehouseByIds(ids);
    }
}
