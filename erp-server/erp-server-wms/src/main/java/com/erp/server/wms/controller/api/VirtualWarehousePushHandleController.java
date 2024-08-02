package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.VirtualWarehousePushHandleService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDTO;

/**
 * 分货单拆单主表
 *
 * @author hyj
 * @since 2024-06-07
 */
@Slf4j
@RestController
@LogSystemModule("分货单拆单主表")
@RequestMapping("/virtualWarehouseAllocationHandle")
public class VirtualWarehousePushHandleController extends BaseController {

    @Resource
    private VirtualWarehousePushHandleService virtualWarehousePushHandleService;

    /**
    * 新增
    * @author hyj
    * @date:  2024-06-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "分货单拆单主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualWarehousePushHandleDTO.AddDTO dto) {
        return success(virtualWarehousePushHandleService.add(dto));
    }

    /**
    * 修改
    * @author hyj
    * @date:  2024-06-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "分货单拆单主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:virtualWarehouseAllocationHandle:update",
        serviceClass = VirtualWarehousePushHandleService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualWarehousePushHandleDTO.UpdateDTO dto) {
        virtualWarehousePushHandleService.update(dto);
        return success();
    }



}
