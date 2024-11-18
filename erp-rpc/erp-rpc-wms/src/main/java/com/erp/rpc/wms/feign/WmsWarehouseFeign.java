package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.WarehouseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 仓库Feign
 *
 * @author Jim
 * @date 2023/11/29
 */
@FeignClient(name = "erp-wms", contextId = "warehouse", path = "/feign/warehouse")
public interface WmsWarehouseFeign {

    /**
     * 根据IDS返回仓库信息，空返回所有已启用的仓库
     *
     * @author Jim
     * @date 2023/11/29
     */
    @PostMapping("/listByIds")
    List<WarehouseDTO.ListDTO> listByIds(@RequestBody List<String> ids);
}


