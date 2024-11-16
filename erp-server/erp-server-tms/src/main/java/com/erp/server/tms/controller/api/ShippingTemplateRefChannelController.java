package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.ShippingTemplateRefChannelDTO;
import com.erp.server.tms.service.ShippingTemplateRefChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 运费模板渠道关联表
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@RestController
@LogSystemModule("运费模板渠道关联表")
@RequestMapping("/shippingTemplateRefChannel")
public class ShippingTemplateRefChannelController extends BaseController {

    @Resource
    private ShippingTemplateRefChannelService shippingTemplateRefChannelService;

    /**
    * 新增
    * @author Will
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "运费模板渠道关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ShippingTemplateRefChannelDTO.AddDTO dto) {
//        return success(shippingTemplateRefChannelService.add(dto));
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
        menuCode = "tms:shippingTemplateRefChannel:update",
        serviceClass = ShippingTemplateRefChannelService.class,
        keyIdName = "id")
    public ApiResult<Object>update(@RequestBody @Validated ShippingTemplateRefChannelDTO.UpdateDTO dto) {
//        shippingTemplateRefChannelService.update(dto);
        return success();
    }



}
