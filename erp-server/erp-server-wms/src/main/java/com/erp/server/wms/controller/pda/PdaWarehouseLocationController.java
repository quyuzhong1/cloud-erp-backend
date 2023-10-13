package com.erp.server.wms.controller.pda;


import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationAreaTypeEnum;
import com.erp.model.wms.enums.WarehouseLocationStatusEnum;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.server.wms.service.WarehouseLocationService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
     * 初始化部分仓位数据
     * @return
     */
    @PostMapping(value = "/getWarehouseArea")
    public ApiResult<Void> getWarehouseArea(@RequestBody ValidList<String> validList) {
//        warehouseLocationService.getWarehouseArea(validList.getList());
        return success();
    }

    /**
     * 初始化部分仓位数据
     * @return
     */
    @PostMapping(value = "/init")
    public ApiResult<Void> init(@RequestParam(value = "warehouseId")String warehouseId,
                                @RequestParam(value = "prefix")String prefix) {
        // 新增分区
        WarehouseLocationEntity warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.AREA.getCode());
        warehouseLocationEntity.setCode(WarehouseLocationAreaTypeEnum.PICK.getCode());
        warehouseLocationEntity.setName("暂存区");
        warehouseLocationEntity.setStatus("");
        warehouseLocationService.save(warehouseLocationEntity);
        String areaId = warehouseLocationEntity.getId();

        // 新增仓位
        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("");
        warehouseLocationEntity.setName("空仓位");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        return success();
    }

}
