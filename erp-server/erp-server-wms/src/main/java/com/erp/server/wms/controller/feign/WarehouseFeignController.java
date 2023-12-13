package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/17 16:01
 */
@RestController
@RequestMapping("feign/warehouse")
public class WarehouseFeignController {

    @Resource
    private WarehouseService  warehouseService;

    @PostMapping("/listWarehouseByIds")
    public List<WarehouseDTO.UpdateDTO> listWarehouseByIds(@RequestBody List<String> ids) {
        return warehouseService.listWarehouseByIds(ids);
    }

    /**
     * 查询所有审核通过并启用的仓库
     * @author Will
     * @date: 2023/3/21 14:26
     * @return List<WarehouseDTO>
     */
    @GetMapping("/listApproveWarehouse")
    public List<WarehouseDTO.ListDTO> listApproveWarehouse() {
        return warehouseService.listApproveWarehouse();
    }

    /**
     * 根据金蝶仓库code 获取到对应仓库信息
     * @Author Luo_WG
     * @Date 2023/6/28 9:45
     * @param kingdeeWarehouseCodeList kingdeeWarehouseCodeList
     * @return java.util.List<com.erp.model.wms.entity.WarehouseEntity>
     **/
    @PostMapping("/listByKingdeeCodeList")
    public List<WarehouseEntity> listByKingdeeCodeList(@RequestBody List<String> kingdeeWarehouseCodeList) {
        return warehouseService.listByKingdeeCodeList(kingdeeWarehouseCodeList);
    }

    /**
     * 根据IDS返回仓库信息，空返回所有已启用的仓库
     *
     * @author Jim
     * @date 2023/11/29
     */
    @PostMapping("/listByIds")
    public List<WarehouseDTO.ListDTO> listByIds(@RequestBody List<String> ids){
        WarehouseDTO.ListParamDTO dto = new WarehouseDTO.ListParamDTO();
        dto.setWarehouseIdList(ids);
        return warehouseService.listWarehouseByParams(dto);
    }
}
