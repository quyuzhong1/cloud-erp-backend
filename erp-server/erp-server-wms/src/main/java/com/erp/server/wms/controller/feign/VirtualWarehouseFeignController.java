package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.server.wms.service.VirtualWarehouseChannelService;
import com.erp.server.wms.service.VirtualWarehouseRelationService;
import com.erp.server.wms.service.VirtualWarehouseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author hyj
 */
@RestController
@RequestMapping("feign/virtualWarehouse")
public class VirtualWarehouseFeignController {

    @Resource
    VirtualWarehouseService virtualWarehouseService;

    @Resource
   private VirtualWarehouseChannelService virtualWarehouseChannelService;

    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;

    /**
     * 根据IDS返回仓库信息
     *
     * @author hyj
     */
    @PostMapping("/listByIds")
    public List<VirtualWarehouseEntity> listByIds(@RequestBody List<String> ids){
        return virtualWarehouseService.listByIds(ids);
    }

    /**
     * 根据关联id、平台、实体仓库id查询
     * @author will
     * @date 2024/6/12 14:24
     * @param platformDTO
     * @return VirtualWarehouseChannelEntity
     */
    @PostMapping("/getVirtualWarehouse")
    public List<VirtualWarehouseRelationEntity> getVirtualWarehouse(@RequestBody @Valid VirtualWarehouseChannelDTO.PlatformDTO platformDTO){
        return virtualWarehouseChannelService.getVirtualWarehouse(platformDTO);
    }

    /**
     * 根据平台查询虚拟仓配置
     * @author will
     * @date 2024/9/3 18:12
     * @param platformList
     * @return List<CfgRuleVirtualWarehouseDTO>
     */
    @PostMapping("/listCfgRuleVirtualWarehouse")
    public List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> listCfgRuleVirtualWarehouse(@RequestBody List<String> platformList){
        return virtualWarehouseChannelService.listCfgRuleVirtualWarehouse(platformList);
    }

    /**
     * 高级查询虚拟仓
     * @param compareCodeSplicingValueSql 高级查询参数
     */
    @GetMapping("/listWarehouseBySql")
    public List<String> listWarehouseBySql(@RequestParam String compareCodeSplicingValueSql){
        return virtualWarehouseService.listWarehouseBySql(compareCodeSplicingValueSql);
    }

    /**
     * 根据仓库ID列表查询虚拟仓关联关系
     * @param warehouseIdList 仓库ID列表
     * @return List<VirtualWarehouseRelationEntity>
     */
    @PostMapping("/getByWarehouseIds")
    public List<VirtualWarehouseRelationEntity> getByWarehouseIds(@RequestBody List<String> warehouseIdList){
        return virtualWarehouseRelationService.getByWarehouseId(warehouseIdList);
    }
}

