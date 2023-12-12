package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.ShippingTemplateCostSettingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.ShippingTemplateCostSettingDTO;

/**
 * 运费模板其他费用选值表
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@RestController
@LogSystemModule("运费模板其他费用选值表")
@RequestMapping("/shippingTemplateCostSetting")
public class ShippingTemplateCostSettingController extends BaseController {

    @Autowired
    private ShippingTemplateCostSettingService shippingTemplateCostSettingService;

    /**
    * 新增
    * @author Will
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "运费模板其他费用选值表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ShippingTemplateCostSettingDTO.AddDTO dto) {
//        return success(shippingTemplateCostSettingService.add(dto));
        return success();
    }

    /**
    * 修改
    * @author Will
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "运费模板其他费用选值表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:shippingTemplateCostSetting:update",
        serviceClass = ShippingTemplateCostSettingService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated ShippingTemplateCostSettingDTO.UpdateDTO dto) {
//        shippingTemplateCostSettingService.update(dto);
        return success();
    }



}
