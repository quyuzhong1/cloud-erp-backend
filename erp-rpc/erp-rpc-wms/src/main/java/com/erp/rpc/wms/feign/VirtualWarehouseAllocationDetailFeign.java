package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 虚拟仓分货单明细Feign
 *
 * @author hyj
 */
@FeignClient(name = "erp-wms", contextId = "virtualWarehouseAllocationDetail", path = "/feign/virtualWarehouseAllocationDetail")
public interface VirtualWarehouseAllocationDetailFeign {

    /**
     * 修改同步状态
     */
    @PostMapping("/updateSyncStatus")
    void updateSyncStatus(@RequestBody VirtualWarehouseAllocationDTO.SyncUpdateDto dto);
}


