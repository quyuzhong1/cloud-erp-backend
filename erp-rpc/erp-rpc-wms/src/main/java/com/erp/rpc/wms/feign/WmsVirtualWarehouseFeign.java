package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}


