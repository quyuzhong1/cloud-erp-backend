package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.entity.FbaInventoryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import com.erp.model.wms.dto.FbaInventoryDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "fbaInventoryFeign",configuration = {FeignErrorDecoder.class})
public interface WmsFbaInventoryFeign {

    /**
     * 批量保存FBA库存信息和预留明细
     *
     * @Author Jim
     * @Date 2023-11-08
     **/
    @PostMapping("/feign/fbaInventory/allBatchSave")
    Boolean allBatchSave(@RequestBody List<FbaInventoryEntity> inventoryEntityList);

    /**
     * 获取FBA库存信息
     *
     * @Author zdy
     * @Date 2025-08-20
     **/
    @PostMapping("/feign/fbaInventory/listFbaInventory")
    List<FbaInventoryDTO.InventoryDTO> listFbaInventory(@RequestBody FbaInventoryDTO.QueryDTO queryDTO);
}
