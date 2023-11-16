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
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;

/**
 * 海外仓入库单
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("海外仓入库单")
@RequestMapping("/overseasWarehouseInbound")
public class OverseasWarehouseInboundController extends BaseController {

    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;

    /**
    * 新增
    * @author Jim
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "海外仓入库单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated OverseasWarehouseInboundDTO.AddDTO dto) {
        return success(overseasWarehouseInboundService.add(dto));
    }

    /**
    * 修改
    * @author Jim
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "海外仓入库单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:overseasWarehouseInbound:update",
        serviceClass = OverseasWarehouseInboundService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated OverseasWarehouseInboundDTO.UpdateDTO dto) {
        overseasWarehouseInboundService.update(dto);
        return success();
    }



}
