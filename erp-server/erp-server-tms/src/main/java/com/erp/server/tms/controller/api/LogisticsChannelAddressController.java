package com.erp.server.tms.controller.api;


import com.erp.model.tms.dto.LogisticsChannelAddressDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsChannelAddressService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

/**
 * 渠道地址表
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("渠道地址表")
@RequestMapping("/logisticsChannelAddress")
public class LogisticsChannelAddressController extends BaseController {

    @Autowired
    private LogisticsChannelAddressService logisticsChannelAddressService;



    /**
    * 修改
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsChannelAddress:update",
        serviceClass = LogisticsChannelAddressService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsChannelAddressDTO.UpdateDTO dto) {
        logisticsChannelAddressService.update(dto);
        return success();
    }



}
