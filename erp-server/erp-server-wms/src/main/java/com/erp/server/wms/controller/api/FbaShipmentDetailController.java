package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.FbaShipmentDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.FbaShipmentDetailDTO;

/**
 * FBI拣货明细表
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@RestController
@LogSystemModule("FBI拣货明细表")
@RequestMapping("/fbaShipmentDetail")
public class FbaShipmentDetailController extends BaseController {

    @Autowired
    private FbaShipmentDetailService fbaShipmentDetailService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "FBI拣货明细表新增")
    public ApiResult<String> add(@RequestBody @Validated FbaShipmentDetailDTO.AddDTO dto) {
        return success(fbaShipmentDetailService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:fbaShipmentDetail:update",
        serviceClass = FbaShipmentDetailService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated FbaShipmentDetailDTO.UpdateDTO dto) {
        fbaShipmentDetailService.update(dto);
        return success();
    }



}
