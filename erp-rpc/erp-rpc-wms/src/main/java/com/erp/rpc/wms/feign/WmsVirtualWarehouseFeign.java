package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}


