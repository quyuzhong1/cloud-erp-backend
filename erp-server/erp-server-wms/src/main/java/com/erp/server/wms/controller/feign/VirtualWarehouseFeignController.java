package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.service.VirtualWarehouseRelationService;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hyj
 */
@RestController
@RequestMapping("feign/virtualWarehouse")
public class VirtualWarehouseFeignController {

    @Resource
    VirtualWarehouseService virtualWarehouseService;

    /**
     * 根据IDS返回仓库信息
     *
     * @author hyj
     */
    @PostMapping("/listByIds")
    public List<VirtualWarehouseEntity> listByIds(@RequestBody List<String> ids){
        return virtualWarehouseService.listByIds(ids);
    }
}

