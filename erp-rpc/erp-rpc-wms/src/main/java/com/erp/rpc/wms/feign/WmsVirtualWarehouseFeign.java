package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;


/**
 * 虚拟仓库Feign
 *
 * @author hyj
 */
@FeignClient(name = "erp-wms", contextId = "virtualWarehouse", path = "/feign/virtualWarehouse")
public interface WmsVirtualWarehouseFeign {

    /**
     * 根据IDS返回仓库信息
     *
     * @author hyj
     */
    @PostMapping("/listByIds")
    List<VirtualWarehouseEntity> listByIds(@RequestBody List<String> ids);

    /**
     * 根据关联id、平台、实体仓库id查询
     * @author will
     * @date 2024/6/12 14:29
     * @param platformDTO
     * @return List<VirtualWarehouseRelationEntity>
     */
    @PostMapping("/getVirtualWarehouse")
    List<VirtualWarehouseRelationEntity> getVirtualWarehouse(@RequestBody @Valid VirtualWarehouseChannelDTO.PlatformDTO platformDTO);

    /**
     * 根据平台查询虚拟仓配置信息
     * @author will
     * @date 2024/9/3 18:09
     * @param platformList
     * @return List<VirtualWarehouseDTO>
     */
    @PostMapping("/listCfgRuleVirtualWarehouse")
    List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> listCfgRuleVirtualWarehouse(@RequestBody List<String> platformList);

    /**
     * 高级查询虚拟仓
     * @param compareCodeSplicingValueSql 高级查询参数
     */
    @GetMapping("/listWarehouseBySql")
    List<String> listWarehouseBySql(@RequestParam String compareCodeSplicingValueSql);

    /**
     * 根据仓库ID列表查询虚拟仓关联关系
     * @param warehouseIdList 仓库ID列表
     * @return List<VirtualWarehouseRelationEntity>
     */
    @PostMapping("/getByWarehouseIds")
    List<VirtualWarehouseRelationEntity> getByWarehouseIds(@RequestBody List<String> warehouseIdList);

}


