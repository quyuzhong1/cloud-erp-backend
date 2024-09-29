package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "fbaInventoryFeign")
public interface WmsFbaInventoryFeign {

    /**
     * 批量保存FBA库存信息和预留明细
     *
     * @Author Jim
     * @Date 2023-11-08
     **/
    @PostMapping("/feign/fbaInventory/allBatchSave")
    Boolean allBatchSave(@RequestBody List<FbaInventoryEntity> inventoryEntityList);
}
