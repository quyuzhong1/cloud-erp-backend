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
import com.erp.server.wms.service.SoB2cDeliveryInterceptService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;

/**
 * b2c发货拦截单
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@RestController
@LogSystemModule("b2c发货拦截单")
@RequestMapping("/soB2cDeliveryIntercept")
public class SoB2cDeliveryInterceptController extends BaseController {

    @Resource
    private SoB2cDeliveryInterceptService soB2cDeliveryInterceptService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-12-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "b2c发货拦截单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoB2cDeliveryInterceptDTO.AddDTO dto) {
        return success(soB2cDeliveryInterceptService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-12-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "b2c发货拦截单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:soB2cDeliveryIntercept:update",
        serviceClass = SoB2cDeliveryInterceptService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoB2cDeliveryInterceptDTO.UpdateDTO dto) {
        soB2cDeliveryInterceptService.update(dto);
        return success();
    }



}
