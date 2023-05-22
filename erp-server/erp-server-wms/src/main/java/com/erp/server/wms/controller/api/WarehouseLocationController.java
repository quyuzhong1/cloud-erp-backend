package com.erp.server.wms.controller.api;


import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationAreaTypeEnum;
import com.erp.model.wms.enums.WarehouseLocationStatusEnum;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.server.wms.service.WarehouseLocationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import java.util.List;

/**
 * 仓库仓位分区
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
@AllArgsConstructor
@RestController
@RequestMapping("/warehouseLocation")
public class WarehouseLocationController extends BaseController {

    private final WarehouseLocationService warehouseLocationService;

    /**
     * 获取仓位下拉列表
     * @return
     */
    @PostMapping(value = "/select")
    public ApiResult<List<WarehouseLocationDTO.LocationListDTO>> select(@RequestParam(value = "warehouseId")String warehouseId) {
        return success(warehouseLocationService.select(warehouseId));
    }

    /**
     * 初始化部分仓位数据
     * @return
     */
    /*
    @PostMapping(value = "/init")
    public ApiResult<Void> init(@RequestParam(value = "warehouseId")String warehouseId) {
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
        warehouseLocationEntity.setCode("C000001");
        warehouseLocationEntity.setName("");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C000002");
        warehouseLocationEntity.setName("");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C000003");
        warehouseLocationEntity.setName("");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C000004");
        warehouseLocationEntity.setName("");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);
        return success();
    }
     */



}
