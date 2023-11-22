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
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;

/**
 * 海外物流商仓库
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("海外物流商仓库")
@RequestMapping("/overseasProviderWarehouse")
public class OverseasProviderWarehouseController extends BaseController {

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "海外物流商仓库修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:overseasProviderWarehouse:update",
        serviceClass = OverseasProviderWarehouseService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated OverseasProviderWarehouseDTO.UpdateDTO dto) {
        overseasProviderWarehouseService.update(dto);
        return success();
    }



}
