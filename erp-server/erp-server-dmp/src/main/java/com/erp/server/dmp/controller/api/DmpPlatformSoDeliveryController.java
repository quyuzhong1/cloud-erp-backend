package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.DmpPlatformSoDeliveryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpPlatformSoDeliveryDTO;

/**
 * 
 *
 * @author zdy
 * @since 2025-08-29
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/dmpPlatformSoDelivery")
public class DmpPlatformSoDeliveryController extends BaseController {

    @Resource
    private DmpPlatformSoDeliveryService dmpPlatformSoDeliveryService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpPlatformSoDeliveryDTO.AddDTO dto) {
        return success(dmpPlatformSoDeliveryService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpPlatformSoDelivery:update",
        serviceClass = DmpPlatformSoDeliveryService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpPlatformSoDeliveryDTO.UpdateDTO dto) {
        dmpPlatformSoDeliveryService.update(dto);
        return success();
    }



}
