package com.erp.server.wms.controller.pda;

import com.common.business.validator.ValidList;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.server.wms.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:库存查询
 * @Author Luo_WG
 * @Date 2023/8/10 10:08
 **/
@RestController
@RequestMapping(value = "/pdaInventory")
public class PdaInventoryController extends BaseController {
    @Resource
    private InventoryService inventoryService;

    /**
     * PDA:首页，根据仓库id查询库存信息
     * @Author Luo_WG
     * @Date 2023/8/10 10:11
     * @param warehouseId
     * @return com.common.core.controller.vo.ApiResult<java.lang.Integer>
     **/
    @GetMapping(value = "/getInventoryByWarehouseId")
    public ApiResult<InventoryDTO.PdaHomeInventoryBalanceDTO> getInventoryByWarehouseId(@RequestParam("warehouseId") String warehouseId) {
        InventoryDTO.PdaHomeInventoryBalanceDTO inventory = inventoryService.getInventoryByWarehouseId(warehouseId);
        return success(inventory);
    }

    /**
     * 根据条件查询库存信息
     * @Author Luo_WG
     * @Date 2023/8/25 11:20
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.inventory.InventoryDTO.PdaSearchParamDTO>
     **/
    @PostMapping(value = "/getInventoryByParam")
    public ApiResult<List<InventoryDTO.PdaInventoryDTO>> getInventoryByParam(@RequestBody InventoryDTO.PdaSearchParamDTO dto) {
        List<InventoryDTO.PdaInventoryDTO> inventorys = inventoryService.getInventoryByParam(dto);
        return success(inventorys);
    }

    /**
     * 根据条件查询库存信息
     * @author hyj
     * @date 2024/4/17 10:57
     * @param dtos
     */
    @PostMapping(value = "/getInventoryQty")
    public ApiResult<List<InventoryDTO.InventoryQtyDTO>> getInventoryByParam(@RequestBody ValidList<InventoryDTO.InventoryBySkuIdAndWarehouseDTO> dtos) {
        return success(inventoryService.getInventoryQty(dtos.getList()));
    }

    /**
     * PDA:库存查询
     * @Author Luo_WG
     * @Date 2023/8/30 11:39
     * @param skuNo
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.inventory.InventoryDTO.PdaInventorySearch>
     **/
    @GetMapping(value = "/getInventoryBySkuNo")
    public ApiResult<InventoryDTO.PdaInventorySearch> getInventoryBySkuNo(@RequestParam("skuNo") String skuNo) {
        InventoryDTO.PdaInventorySearch inventory = inventoryService.getInventoryBySkuNo(skuNo);
        return success(inventory);
    }


}
