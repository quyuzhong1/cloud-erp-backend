package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.WarehouseMappingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.WarehouseMappingDTO;

/**
 * 仓库映射第三方平台表
 *
 * @author Luo_WG
 * @since 2024-01-30
 */
@Slf4j
@RestController
@LogSystemModule("仓库映射第三方平台表")
@RequestMapping("/warehouseMapping")
public class WarehouseMappingController extends BaseController {

    @Resource
    private WarehouseMappingService warehouseMappingService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2024-01-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "仓库映射第三方平台表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated WarehouseMappingDTO.AddDTO dto) {
        return success(warehouseMappingService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2024-01-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "仓库映射第三方平台表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:warehouseMapping:update",
        serviceClass = WarehouseMappingService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WarehouseMappingDTO.UpdateDTO dto) {
        warehouseMappingService.update(dto);
        return success();
    }



}
