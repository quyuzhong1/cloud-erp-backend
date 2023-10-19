package com.erp.server.wms.controller.pda;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PdaWarehouseLocationDTO;
import com.erp.server.wms.service.WarehouseLocationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PDA:仓位管理
 * @Author Luo_WG
 * @Date 2023/10/13 14:23
 **/
@AllArgsConstructor
@RestController
@RequestMapping("/pdaWarehouseLocation")
public class PdaWarehouseLocationController extends BaseController {

    private final WarehouseLocationService warehouseLocationService;

    /**
     * 获取仓库下的区位信息
     * @return
     */
    @GetMapping(value = "/listWarehouseArea")
    public ApiResult<List<PdaWarehouseLocationDTO.WarehouseAreaDTO>> listWarehouseArea() {
        List<PdaWarehouseLocationDTO.WarehouseAreaDTO> list = warehouseLocationService.listWarehouseArea();
        return success(list);
    }

    /**
     * 新增仓位
     * @Author Luo_WG
     * @Date 2023/10/17 17:33
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/addWarehouseLocation")
    public ApiResult addWarehouseLocation(@RequestBody PdaWarehouseLocationDTO.WarehouseLocationAddDTO dto) {
        Boolean flag = warehouseLocationService.addWarehouseLocation(dto);
        return flag ? success() : failure();
    }

}
