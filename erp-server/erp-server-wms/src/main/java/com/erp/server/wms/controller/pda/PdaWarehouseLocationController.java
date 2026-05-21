package com.erp.server.wms.controller.pda;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.PdaWarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
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
@LogSystemModule("PDA仓位管理")
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
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仓位")
    @PostMapping(value = "/addWarehouseLocation")
    public ApiResult addWarehouseLocation(@RequestBody PdaWarehouseLocationDTO.WarehouseLocationAddDTO dto) {
        Boolean flag = warehouseLocationService.addWarehouseLocation(dto);
        return flag ? success() : failure();
    }

    /**
     * 查询东莞售后仓库下的所有仓位列表
     *
     * @return List<WarehouseLocationDTO.LocationSelectDTO>
     */
    @GetMapping(value = "/listByAfterSalesWarehouse")
    public ApiResult<List<WarehouseLocationDTO.ViewDto>> listByAfterSalesWarehouse(@RequestParam("name") String name) {
        return success(warehouseLocationService.listByAfterSalesWarehouse(name));
    }

    /**
     * 根据仓位编号查询仓位信息
     *
     * @param code 仓库编码
     * @return WarehouseLocationEntity
     */
    @GetMapping(value = "/getByCode")
    public ApiResult<WarehouseLocationEntity> getByCode(@RequestParam("code") String code) {
        return success(warehouseLocationService.getByCode(code));
    }

}
