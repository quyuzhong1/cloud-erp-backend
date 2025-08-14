package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;


/**
 * 虚拟库存
 * @author will
 * @date 2024/6/13 16:34
 */
@FeignClient(name = "erp-wms", contextId = "virtualInventory", path = "/feign/virtualInventory",configuration = {FeignErrorDecoder.class})
public interface VirtualInventoryFeign {


    /**
     * 查询虚拟库存
     * @author will
     * @date 2024/6/13 16:34
     * @param paramDTO
     * @return List<VirtualInventoryQtyDTO>
     */
    @PostMapping("/listInventoryQty")
    List<VirtualInventoryDTO.VirtualInventoryQtyDTO> listInventoryQty(@RequestBody @Valid VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO);

    /**
     * 库存扣减
     * @author will
     * @date 2024/7/16 9:17
     * @param stockParamDTO
     */
    @PostMapping(value = "/approveByType")
    Boolean approveByType(@RequestBody @Valid VirtualInventoryStockDTO.StockParamDTO stockParamDTO);
}


