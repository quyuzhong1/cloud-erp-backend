package com.erp.server.dmp.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.CfgAppClientService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.CfgAppClientDTO;

/**
 * 第三方应用程序信息表
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/cfgAppClient")
@LogSystemModule("第三方应用程序信息表")
public class CfgAppClientController extends BaseController {

    @Autowired
    private CfgAppClientService cfgAppClientService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<String> add(@RequestBody @Validated CfgAppClientDTO.AddDTO dto) {
        return success(cfgAppClientService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    public ApiResult update(@RequestBody @Validated CfgAppClientDTO.UpdateDTO dto) {
        cfgAppClientService.update(dto);
        return success();
    }



}
