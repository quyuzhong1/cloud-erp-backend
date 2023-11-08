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
import com.erp.server.tms.service.ShippingTemplateRuleService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.ShippingTemplateRuleDTO;

/**
 * 运费模板渠道关联表
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@RestController
@LogSystemModule("运费模板渠道关联表")
@RequestMapping("/shippingTemplateRule")
public class ShippingTemplateRuleController extends BaseController {

    @Autowired
    private ShippingTemplateRuleService shippingTemplateRuleService;

    /**
    * 新增
    * @author Will
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "运费模板渠道关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ShippingTemplateRuleDTO.AddDTO dto) {
//        return success(shippingTemplateRuleService.add(dto));
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "运费模板渠道关联表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:shippingTemplateRule:update",
        serviceClass = ShippingTemplateRuleService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated ShippingTemplateRuleDTO.UpdateDTO dto) {
//        shippingTemplateRuleService.update(dto);
        return success();
    }



}
