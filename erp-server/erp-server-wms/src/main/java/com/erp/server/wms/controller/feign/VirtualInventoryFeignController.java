package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.server.wms.service.VirtualInventoryService;
import com.erp.server.wms.service.VirtualInventoryTransCoreService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 虚拟库存
 * @author will
 * @date 2024/6/13 16:31
 */
@RestController
@RequestMapping("feign/virtualInventory")
public class VirtualInventoryFeignController {

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    /**
     * 查询虚拟仓库存
     * @author will
     * @date 2024/6/13 16:33
     * @param paramDTO
     * @return List<VirtualInventoryQtyDTO>
     */
    @PostMapping("/listInventoryQty")
    public List<VirtualInventoryDTO.VirtualInventoryQtyDTO> listInventoryQty(@RequestBody @Valid VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO){
        return virtualInventoryService.listInventoryQty(paramDTO);
    }


    /**
     * 库存扣减
     * @author will
     * @date 2024/7/16 9:16
     * @param stockParamDTO
     * @return Boolean
     */
    @PostMapping("/approveByType")
    public Boolean approveByType(@RequestBody @Valid VirtualInventoryStockDTO.StockParamDTO stockParamDTO) {
        virtualInventoryTransCoreService.approve(stockParamDTO);
        return Boolean.TRUE;
    }
}

